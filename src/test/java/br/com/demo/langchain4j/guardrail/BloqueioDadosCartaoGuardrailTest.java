package br.com.demo.langchain4j.guardrail;

import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guardrail é código Java comum: dá para testar sem chamar nenhum LLM. */
class BloqueioDadosCartaoGuardrailTest {

    private final BloqueioDadosCartaoGuardrail guardrail = new BloqueioDadosCartaoGuardrail();

    @Test
    void bloqueiaNumeroDeCartaoValido() {
        // número de teste público (passa no Luhn)
        var resultado = guardrail.validate(UserMessage.from("Meu cartão é 4111 1111 1111 1111, está bloqueado?"));
        assertFalse(resultado.isSuccess());
    }

    @Test
    void permiteQuatroUltimosDigitos() {
        var resultado = guardrail.validate(UserMessage.from("Meu cartão final 1111 foi recusado"));
        assertTrue(resultado.isSuccess());
    }

    @Test
    void naoConfundeSequenciaQueNaoPassaNoLuhn() {
        assertFalse(BloqueioDadosCartaoGuardrail.contemNumeroDeCartao("Protocolo 1234567890123"));
    }
}
