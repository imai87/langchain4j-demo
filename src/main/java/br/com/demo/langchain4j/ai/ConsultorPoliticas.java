package br.com.demo.langchain4j.ai;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * PASSO 4 — RAG (Retrieval-Augmented Generation).
 * Antes de chamar o modelo, o ContentRetriever busca os trechos mais relevantes
 * das políticas internas e os injeta no prompt. O modelo responde com base
 * no NOSSO conhecimento, não só no que aprendeu no treinamento.
 */
@AiService(
        wiringMode = EXPLICIT,
        chatModel = "openAiChatModel",
        contentRetriever = "politicasRetriever"
)
public interface ConsultorPoliticas {

    @SystemMessage("""
            Você responde dúvidas de analistas sobre as políticas internas de prevenção à fraude.
            Use SOMENTE as informações fornecidas no contexto. Se a resposta não estiver lá,
            diga claramente que não encontrou nas políticas. Cite o nome do documento usado.
            Responda em português do Brasil, de forma direta.
            """)
    Result<String> perguntar(@UserMessage String pergunta);
}
