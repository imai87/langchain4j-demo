package br.com.demo.langchain4j.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Etapa de INGESTÃO do RAG, executada na subida da aplicação:
 * documento -> pedaços (chunks) -> vetores (embeddings) -> banco vetorial.
 */
@Component
public class IngestaoPoliticas implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(IngestaoPoliticas.class);

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    public IngestaoPoliticas(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        List<Document> documentos = carregarDocumentos();

        EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(500, 80))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build()
                .ingest(documentos);

        log.info("RAG: {} documento(s) de política indexado(s)", documentos.size());
    }

    private List<Document> carregarDocumentos() throws IOException {
        Resource[] arquivos = new PathMatchingResourcePatternResolver()
                .getResources("classpath:politicas/*.md");

        List<Document> documentos = new ArrayList<>();
        for (Resource arquivo : arquivos) {
            String conteudo = arquivo.getContentAsString(StandardCharsets.UTF_8);
            documentos.add(Document.from(conteudo, Metadata.from("arquivo", arquivo.getFilename())));
        }
        return documentos;
    }
}
