package br.com.demo.langchain4j.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * PASSO 2 — AI Service declarativo + memória.
 * Só uma interface: o LangChain4j gera a implementação (proxy) em runtime,
 * monta o prompt, chama o modelo e guarda o histórico por conversa.
 */
@AiService(
        wiringMode = EXPLICIT,
        chatModel = "openAiChatModel",
        chatMemoryProvider = "chatMemoryProvider"
)
public interface AssistenteAtendimento {

    @SystemMessage("""
            Você é a assistente virtual de uma fintech brasileira, falando com clientes.
            Seja cordial, objetiva e responda em português do Brasil, em no máximo 4 frases.
            Nunca peça senha, código de verificação ou número completo de cartão.
            """)
    Result<String> conversar(@MemoryId String conversaId, @UserMessage String mensagem);
}
