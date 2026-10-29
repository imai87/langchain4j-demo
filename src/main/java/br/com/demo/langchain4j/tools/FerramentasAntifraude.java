package br.com.demo.langchain4j.tools;

import br.com.demo.langchain4j.dados.BaseFicticia;
import br.com.demo.langchain4j.dados.BaseFicticia.Cliente;
import br.com.demo.langchain4j.dados.BaseFicticia.EventoAcesso;
import br.com.demo.langchain4j.dados.BaseFicticia.Transacao;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PASSO 5 — Tools (function calling).
 * Métodos Java comuns. A anotação @Tool descreve para o modelo O QUE cada um faz;
 * o LangChain4j gera a especificação, recebe o pedido de chamada do modelo,
 * executa o método e devolve o resultado para o modelo continuar o raciocínio.
 */
@Component
public class FerramentasAntifraude {

    private static final Logger log = LoggerFactory.getLogger(FerramentasAntifraude.class);

    public record SolicitacaoBloqueio(String protocolo, String clienteId, String motivo, String status) {}

    private final BaseFicticia base;
    private final AtomicInteger sequencia = new AtomicInteger(4120);

    public FerramentasAntifraude(BaseFicticia base) {
        this.base = base;
    }

    @Tool("Consulta o perfil cadastral do cliente: nome, cidade habitual, tempo de conta, dispositivo habitual e limite Pix noturno")
    public Cliente consultarPerfilCliente(@P("Identificador do cliente, ex.: 1001") String clienteId) {
        log.info("TOOL consultarPerfilCliente({})", clienteId);
        return base.cliente(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente " + clienteId + " não encontrado"));
    }

    @Tool("Lista as transações recentes do cliente com valor, destino, dispositivo, localização e status")
    public List<Transacao> consultarTransacoesRecentes(@P("Identificador do cliente") String clienteId) {
        log.info("TOOL consultarTransacoesRecentes({})", clienteId);
        return base.transacoes(clienteId);
    }

    @Tool("Lista eventos recentes de acesso e segurança da conta: logins, novos dispositivos, troca de senha")
    public List<EventoAcesso> consultarEventosDeAcesso(@P("Identificador do cliente") String clienteId) {
        log.info("TOOL consultarEventosDeAcesso({})", clienteId);
        return base.acessos(clienteId);
    }

    @Tool("""
            Abre uma SOLICITAÇÃO de bloqueio cautelar da conta, que fica pendente de aprovação de um analista humano.
            Use apenas quando os critérios da política de bloqueio cautelar forem atendidos.
            """)
    public SolicitacaoBloqueio solicitarBloqueioCautelar(
            @P("Identificador do cliente") String clienteId,
            @P("Motivo objetivo, citando os sinais de alerta e a política aplicada") String motivo) {
        log.warn("TOOL solicitarBloqueioCautelar({}, {})", clienteId, motivo);
        String protocolo = "BC-" + sequencia.incrementAndGet();
        return new SolicitacaoBloqueio(protocolo, clienteId, motivo, "PENDENTE_APROVACAO_ANALISTA");
    }
}
