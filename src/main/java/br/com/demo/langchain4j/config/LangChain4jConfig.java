package br.com.demo.langchain4j.config;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * Peças de infraestrutura que os AI Services usam.
 * Cada bean aqui é uma abstração do LangChain4j: trocar a implementação
 * (ex.: memória em Redis, embeddings no pgvector) não muda os AI Services.
 */
@Configuration
public class LangChain4jConfig {

    // ---------------------------------------------------------------- Modelo de chat

    /**
     * Os @AiService apontam para o bean "openAiChatModel" (starter open-ai).
     * A Anthropic fala um protocolo próprio, então no perfil "claude" o starter
     * open-ai é desligado (ver application.yml) e este bean assume o mesmo nome,
     * apontando para o AnthropicChatModel — os @AiService não precisam mudar.
     */
    @Bean(name = "openAiChatModel")
    @Primary
    @Profile("claude")
    ChatModel chatModelClaude(AnthropicChatModel anthropicChatModel) {
        return anthropicChatModel;
    }

    // ---------------------------------------------------------------- Memória

    /** Onde as mensagens ficam guardadas. Em produção: Redis, banco relacional etc. */
    @Bean
    ChatMemoryStore chatMemoryStore() {
        return new ChatMemoryStoreSemConteudoNulo(new InMemoryChatMemoryStore());
    }

    /**
     * Modelos como o gpt-oss respondem com tool calls e sem texto (AiMessage.text() == null).
     * Ao reenviar o histórico, o LangChain4j manda "content": null, e o Ollama recusa com
     * "invalid message content type: <nil>". Aqui o texto nulo vira "" na hora de ler a memória.
     */
    private record ChatMemoryStoreSemConteudoNulo(ChatMemoryStore delegate) implements ChatMemoryStore {

        @Override
        public List<ChatMessage> getMessages(Object memoryId) {
            return delegate.getMessages(memoryId).stream()
                    .map(m -> m instanceof AiMessage ai && ai.text() == null ? ai.withText("") : m)
                    .toList();
        }

        @Override
        public void updateMessages(Object memoryId, List<ChatMessage> messages) {
            delegate.updateMessages(memoryId, messages);
        }

        @Override
        public void deleteMessages(Object memoryId) {
            delegate.deleteMessages(memoryId);
        }
    }

    /** Uma memória por conversa (@MemoryId), mantendo apenas as últimas 20 mensagens. */
    @Bean
    ChatMemoryProvider chatMemoryProvider(ChatMemoryStore store) {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .chatMemoryStore(store)
                .build();
    }

    // ---------------------------------------------------------------- RAG

    /** Modelo de embeddings que roda dentro da JVM (ONNX), sem chamada externa. */
    @Bean
    EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    /** Banco vetorial. Em produção: pgvector, Elasticsearch, OpenSearch, Redis... */
    @Bean
    EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    /** Busca semântica: transforma a pergunta em vetor e traz os trechos mais parecidos. */
    @Bean
    ContentRetriever politicasRetriever(EmbeddingStore<TextSegment> embeddingStore,
                                        EmbeddingModel embeddingModel,
                                        @Value("${demo.rag.max-resultados}") int maxResultados,
                                        @Value("${demo.rag.score-minimo}") double scoreMinimo) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(maxResultados)
                .minScore(scoreMinimo)
                .build();
    }
}
