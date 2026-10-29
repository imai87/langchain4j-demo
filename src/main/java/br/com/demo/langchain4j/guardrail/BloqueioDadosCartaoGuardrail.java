package br.com.demo.langchain4j.guardrail;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PASSO 6 — Guardrail de entrada.
 * Roda ANTES da chamada ao modelo. Se a mensagem contém um número de cartão
 * válido (checagem de Luhn), a requisição é barrada e o dado nunca sai da aplicação.
 * Código Java determinístico, testável com JUnit como qualquer outra regra.
 */
public class BloqueioDadosCartaoGuardrail implements InputGuardrail {

    // 13 a 19 dígitos, permitindo espaços ou hífens entre eles
    private static final Pattern CANDIDATO_CARTAO = Pattern.compile("\\b(?:\\d[ -]?){12,18}\\d\\b");

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        if (contemNumeroDeCartao(userMessage.singleText())) {
            return failure("Mensagem bloqueada: contém um número de cartão completo. "
                    + "Por segurança (PCI/LGPD), informe apenas os 4 últimos dígitos.");
        }
        return success();
    }

    static boolean contemNumeroDeCartao(String texto) {
        Matcher m = CANDIDATO_CARTAO.matcher(texto);
        while (m.find()) {
            String digitos = m.group().replaceAll("[ -]", "");
            if (passaNoLuhn(digitos)) {
                return true;
            }
        }
        return false;
    }

    static boolean passaNoLuhn(String digitos) {
        int soma = 0;
        boolean dobrar = false;
        for (int i = digitos.length() - 1; i >= 0; i--) {
            int d = digitos.charAt(i) - '0';
            if (dobrar) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            soma += d;
            dobrar = !dobrar;
        }
        return soma % 10 == 0;
    }
}
