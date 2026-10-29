package br.com.demo.langchain4j.ai.modelo;

import dev.langchain4j.model.output.structured.Description;

import java.util.List;

/** Estrutura que o modelo é obrigado a devolver no PASSO 3. As descrições viram parte do JSON Schema. */
public record AnaliseRisco(

        @Description("Nível de risco da ocorrência")
        NivelRisco nivel,

        @Description("Score de risco de 0 (nenhum risco) a 100 (fraude quase certa)")
        int score,

        @Description("Tipo provável de golpe, ex.: falsa central, engenharia social, conta laranja, tomada de conta. Use 'nenhum' se não houver indício")
        String tipoProvavelDeGolpe,

        @Description("Sinais de alerta encontrados no relato, cada um em uma frase curta")
        List<String> sinaisDeAlerta,

        @Description("Próxima ação recomendada para o time de prevenção")
        String acaoRecomendada,

        @Description("true se um analista humano precisa revisar antes de qualquer ação")
        boolean exigeRevisaoHumana
) {
}
