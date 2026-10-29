package br.com.demo.langchain4j.dados;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Dados 100% FICTÍCIOS para a demonstração. Em um sistema real, estas consultas
 * seriam chamadas a outros microsserviços (Feign/RestClient), banco ou cache.
 */
@Component
public class BaseFicticia {

    public record Cliente(String id, String nome, String cidadeHabitual, String clienteDesde,
                          String dispositivoHabitual, BigDecimal limitePixNoturno) {}

    public record Transacao(String dataHora, String tipo, BigDecimal valor, String destino,
                            String dispositivo, String localizacao, String status) {}

    public record EventoAcesso(String dataHora, String evento, String dispositivo, String localizacao) {}

    private final Map<String, Cliente> clientes;
    private final Map<String, List<Transacao>> transacoes;
    private final Map<String, List<EventoAcesso>> acessos;

    public BaseFicticia() {
        var fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String hoje = LocalDate.now().format(fmt);
        String ontem = LocalDate.now().minusDays(1).format(fmt);

        clientes = Map.of(
                "1001", new Cliente("1001", "Ana Souza", "São Paulo/SP", "2019",
                        "iPhone 13 (cadastrado há 2 anos)", new BigDecimal("1000.00")),
                "1002", new Cliente("1002", "Bruno Lima", "Recife/PE", "2021",
                        "Motorola Edge (cadastrado há 1 ano)", new BigDecimal("1000.00"))
        );

        transacoes = Map.of(
                "1001", List.of(
                        new Transacao(ontem + " 14:12", "Compra débito", new BigDecimal("23.50"),
                                "Padaria Bom Dia", "iPhone 13", "São Paulo/SP", "APROVADA"),
                        new Transacao(hoje + " 02:47", "Pix", new BigDecimal("1000.00"),
                                "Chave aleatória nova — conta destino aberta há 3 dias", "Galaxy A15 (novo)", "Manaus/AM", "APROVADA"),
                        new Transacao(hoje + " 02:51", "Pix", new BigDecimal("990.00"),
                                "Mesma chave aleatória", "Galaxy A15 (novo)", "Manaus/AM", "NEGADA — limite noturno excedido"),
                        new Transacao(hoje + " 02:58", "Pedido de aumento do limite Pix noturno para R$ 10.000", BigDecimal.ZERO,
                                "-", "Galaxy A15 (novo)", "Manaus/AM", "EM CARÊNCIA")
                ),
                "1002", List.of(
                        new Transacao(ontem + " 12:30", "Compra crédito", new BigDecimal("58.90"),
                                "Restaurante Sabor do Recife", "Motorola Edge", "Recife/PE", "APROVADA"),
                        new Transacao(hoje + " 09:15", "Pix", new BigDecimal("200.00"),
                                "Maria Lima (contato frequente)", "Motorola Edge", "Recife/PE", "APROVADA")
                )
        );

        acessos = Map.of(
                "1001", List.of(
                        new EventoAcesso(hoje + " 02:20", "Redefinição de senha via SMS", "Galaxy A15 (novo)", "Manaus/AM"),
                        new EventoAcesso(hoje + " 02:31", "Novo dispositivo autorizado", "Galaxy A15 (novo)", "Manaus/AM"),
                        new EventoAcesso(ontem + " 14:05", "Login", "iPhone 13", "São Paulo/SP")
                ),
                "1002", List.of(
                        new EventoAcesso(hoje + " 09:10", "Login", "Motorola Edge", "Recife/PE")
                )
        );
    }

    public Optional<Cliente> cliente(String id) {
        return Optional.ofNullable(clientes.get(id));
    }

    public List<Transacao> transacoes(String id) {
        return transacoes.getOrDefault(id, List.of());
    }

    public List<EventoAcesso> acessos(String id) {
        return acessos.getOrDefault(id, List.of());
    }
}
