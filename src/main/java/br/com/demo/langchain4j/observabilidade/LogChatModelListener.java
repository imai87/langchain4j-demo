package br.com.demo.langchain4j.observabilidade;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observabilidade: todo bean ChatModelListener é plugado automaticamente
 * no ChatModel criado pelo starter. Aqui só logamos; em produção, envie
 * para Micrometer/Prometheus/OpenTelemetry (latência, tokens, erros, custo).
 */
@Component
public class LogChatModelListener implements ChatModelListener {

    private static final Logger log = LoggerFactory.getLogger(LogChatModelListener.class);
    private static final String INICIO = "inicio";

    @Override
    public void onRequest(ChatModelRequestContext ctx) {
        ctx.attributes().put(INICIO, System.nanoTime());
        log.info("LLM -> requisição com {} mensagem(ns), {} tool(s) disponível(is)",
                ctx.chatRequest().messages().size(),
                ctx.chatRequest().toolSpecifications() == null ? 0 : ctx.chatRequest().toolSpecifications().size());
    }

    @Override
    public void onResponse(ChatModelResponseContext ctx) {
        long ms = (System.nanoTime() - (long) ctx.attributes().get(INICIO)) / 1_000_000;
        TokenUsage uso = ctx.chatResponse().tokenUsage();
        log.info("LLM <- resposta em {} ms | tokens entrada={} saída={} | finish={}",
                ms,
                uso == null ? "?" : uso.inputTokenCount(),
                uso == null ? "?" : uso.outputTokenCount(),
                ctx.chatResponse().finishReason());
    }

    @Override
    public void onError(ChatModelErrorContext ctx) {
        log.error("LLM !! erro: {}", ctx.error().getMessage());
    }
}
