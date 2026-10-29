package br.com.demo.langchain4j.ai;

import br.com.demo.langchain4j.ai.modelo.AnaliseRisco;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * PASSO 3 — Saída estruturada.
 * O retorno é um record Java: o LangChain4j gera o JSON Schema a partir dele,
 * pede ao modelo uma resposta nesse formato e desserializa automaticamente.
 * Texto livre entra, objeto tipado sai — pronto para regra de negócio, Kafka, banco.
 */
@AiService(wiringMode = EXPLICIT, chatModel = "openAiChatModel")
public interface AnalisadorOcorrencia {

    @SystemMessage("""
            Você é um analista sênior de prevenção à fraude em uma fintech brasileira.
            Classifique objetivamente o relato recebido. Não invente fatos que não estejam no relato.
            """)
    @UserMessage("Relato do cliente ou do atendimento:\n\n{{relato}}")
    Result<AnaliseRisco> analisar(@V("relato") String relato);
}
