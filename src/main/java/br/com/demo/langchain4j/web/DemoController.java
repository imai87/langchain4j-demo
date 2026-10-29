package br.com.demo.langchain4j.web;

import br.com.demo.langchain4j.ai.AgenteAntifraude;
import br.com.demo.langchain4j.ai.AnalisadorOcorrencia;
import br.com.demo.langchain4j.ai.AssistenteAtendimento;
import br.com.demo.langchain4j.ai.ConsultorPoliticas;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrailException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Supplier;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    public record Pergunta(String conversaId, String mensagem) {}

    private final ChatModel chatModel;
    private final AssistenteAtendimento assistente;
    private final AnalisadorOcorrencia analisador;
    private final ConsultorPoliticas consultor;
    private final AgenteAntifraude agente;

    public DemoController(ChatModel chatModel, AssistenteAtendimento assistente, AnalisadorOcorrencia analisador,
                          ConsultorPoliticas consultor, AgenteAntifraude agente) {
        this.chatModel = chatModel;
        this.assistente = assistente;
        this.analisador = analisador;
        this.consultor = consultor;
        this.agente = agente;
    }

    /** PASSO 1 — API de baixo nível: você monta as mensagens e chama o modelo. Sem memória. */
    @PostMapping("/1-chat")
    public RespostaDemo chatBasico(@RequestBody Pergunta p) {
        long inicio = System.currentTimeMillis();
        ChatResponse resposta = chatModel.chat(ChatRequest.builder()
                .messages(
                        SystemMessage.from("Responda em português do Brasil, em no máximo 3 frases."),
                        UserMessage.from(p.mensagem()))
                .build());
        return RespostaDemo.simples(resposta.aiMessage().text(), resposta.tokenUsage(),
                System.currentTimeMillis() - inicio);
    }

    /** PASSO 2 — AI Service + memória por conversa. */
    @PostMapping("/2-assistente")
    public RespostaDemo assistente(@RequestBody Pergunta p) {
        return medir(() -> assistente.conversar(p.conversaId(), p.mensagem()));
    }

    /** PASSO 3 — Texto livre -> objeto Java (AnaliseRisco). */
    @PostMapping("/3-estruturado")
    public RespostaDemo estruturado(@RequestBody Pergunta p) {
        return medir(() -> analisador.analisar(p.mensagem()));
    }

    /** PASSO 4 — RAG sobre as políticas internas. */
    @PostMapping("/4-rag")
    public RespostaDemo rag(@RequestBody Pergunta p) {
        return medir(() -> consultor.perguntar(p.mensagem()));
    }

    /** PASSOS 5 e 6 — Agente com tools, RAG, memória e guardrail. */
    @PostMapping("/5-agente")
    public RespostaDemo agente(@RequestBody Pergunta p) {
        return medir(() -> agente.analisar(p.conversaId(), p.mensagem()));
    }

    @ExceptionHandler(InputGuardrailException.class)
    public ResponseEntity<RespostaDemo> guardrail(InputGuardrailException e) {
        return ResponseEntity.ok(RespostaDemo.bloqueada(e.getMessage(), 0));
    }

    private static RespostaDemo medir(Supplier<Result<?>> chamada) {
        long inicio = System.currentTimeMillis();
        var resultado = chamada.get();
        return RespostaDemo.de(resultado, System.currentTimeMillis() - inicio);
    }
}
