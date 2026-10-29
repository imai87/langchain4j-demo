package br.com.demo.langchain4j.web;

import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.tool.ToolExecution;

import java.util.List;

/**
 * O que a tela mostra: a resposta e os "bastidores" (tokens, latência,
 * tools chamadas e trechos recuperados pelo RAG).
 */
public record RespostaDemo(
        Object resposta,
        Integer tokensEntrada,
        Integer tokensSaida,
        long latenciaMs,
        List<ChamadaTool> tools,
        List<Fonte> fontes,
        String bloqueio
) {

    public record ChamadaTool(String nome, String argumentos, String resultado) {}

    public record Fonte(String arquivo, String trecho) {}

    public static RespostaDemo de(Result<?> r, long latenciaMs) {
        TokenUsage uso = r.tokenUsage();
        List<ChamadaTool> tools = r.toolExecutions() == null ? List.of()
                : r.toolExecutions().stream().map(RespostaDemo::paraChamada).toList();
        List<Fonte> fontes = r.sources() == null ? List.of()
                : r.sources().stream().map(RespostaDemo::paraFonte).toList();
        return new RespostaDemo(r.content(),
                uso == null ? null : uso.inputTokenCount(),
                uso == null ? null : uso.outputTokenCount(),
                latenciaMs, tools, fontes, null);
    }

    public static RespostaDemo simples(String texto, TokenUsage uso, long latenciaMs) {
        return new RespostaDemo(texto,
                uso == null ? null : uso.inputTokenCount(),
                uso == null ? null : uso.outputTokenCount(),
                latenciaMs, List.of(), List.of(), null);
    }

    public static RespostaDemo bloqueada(String motivo, long latenciaMs) {
        return new RespostaDemo(null, 0, 0, latenciaMs, List.of(), List.of(), motivo);
    }

    private static ChamadaTool paraChamada(ToolExecution t) {
        return new ChamadaTool(t.request().name(), t.request().arguments(), t.result());
    }

    private static Fonte paraFonte(Content c) {
        String arquivo = c.textSegment().metadata().getString("arquivo");
        return new Fonte(arquivo == null ? "?" : arquivo, c.textSegment().text());
    }
}
