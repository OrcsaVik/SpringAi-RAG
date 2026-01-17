package com.github.springAi.parse;

import com.github.springAi.domain.IngestionCandidate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final List<DocumentParser> parsers;
    private final TokenTextSplitter textSplitter;
    private final VectorStore vectorStore;

    public void ingestFile(File file) {
        log.info("Starting ingestion for: {}", file.getName());

        // 1. Select Parser
        String extension = getExtension(file.getName());
        DocumentParser parser = parsers.stream()
                .filter(p -> p.supports(null, extension))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No parser found for extension: " + extension));

        // 2. Parse Content
        IngestionCandidate candidate = parser.parse(new FileSystemResource(file));

        // 3. Idempotency Check (SHA256)
        String hash = ContentHasher.computeHash(candidate.getContent());
        candidate.setContentHash(hash);
        
        // TODO: Query VectorStore metadata to see if "hash" == this hash exists.
        // For now, we just log it.
        log.info("Content Hash: {}", hash);

        // 4. Create Spring AI Document & Add Metadata
        Document doc = candidate.toDocument();
        doc.getMetadata().put("sha256", hash);
        doc.getMetadata().put("file_name", file.getName());

        // 5. Chunking
        List<Document> splitDocuments = textSplitter.apply(List.of(doc));
        log.info("Split into {} chunks", splitDocuments.size());

        // 6. Embed & Store (Qdrant)
        vectorStore.add(splitDocuments);
        
        log.info("Ingestion complete for {}", file.getName());
    }

    private String getExtension(String filename) {
        int i = filename.lastIndexOf('.');
        return (i > 0) ? filename.substring(i + 1) : "";
    }
}