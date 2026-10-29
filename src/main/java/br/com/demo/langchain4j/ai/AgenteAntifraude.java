package br.com.demo.langchain4j.ai;

import br.com.demo.langchain4j.guardrail.BloqueioDadosCartaoGuardrail;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * PASSOS 5 e 6 — Agente: Tools + RAG + memória + guardrail.
 * O modelo decide QUAIS métodos Java chamar (e com quais argumentos) para
 * buscar fatos, consulta as políticas via RAG e propõe uma ação.
 * O guardrail roda ANTES do modelo e barra dados de cartão na entrada.
 */
@AiService(
        wiringMode = EXPLICIT,
        chatModel = "openAiChatModel",
        chatMemoryProvider = "chatMemoryProvider",
        contentRetriever = "politicasRetriever",
        tools = {"ferramentasAntifraude"}
)
@InputGuardrails(BloqueioDadosCartaoGuardrail.class)
public interface AgenteAntifraude {

    @SystemMessage("""
            Você é um copiloto para analistas de prevenção à fraude de uma fintech brasileira.
            Regras:
            1. Use as ferramentas para obter fatos sobre o cliente. Nunca invente dados.
            2. Siga as políticas internas fornecidas no contexto e cite o documento que embasou a decisão.
            3. Você NÃO bloqueia contas: no máximo SOLICITA bloqueio cautelar, que depende de aprovação de um analista.
            4. Responda em português do Brasil com: resumo do caso, sinais encontrados, decisão e próximo passo.
            """)
    Result<String> analisar(@MemoryId String conversaId, @UserMessage String pedido);
}
