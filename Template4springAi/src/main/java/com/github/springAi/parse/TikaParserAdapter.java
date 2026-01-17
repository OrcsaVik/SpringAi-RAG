package com.github.springAi.parse;


import com.github.springAi.domain.IngestionCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TikaParserAdapter implements DocumentParser {

    @Override
    public boolean supports(String mimeType, String extension) {
        return "pdf".equalsIgnoreCase(extension) || "docx".equalsIgnoreCase(extension);
    }

    @Override
    public IngestionCandidate parse(Resource resource) {
        // Use Spring AI's Tika Reader
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> documents = reader.get();
        
        // Join pages if Tika splits them, or take the first one
        StringBuilder content = new StringBuilder();
        documents.forEach(doc -> content.append(doc.getContent()).append("\n"));

        return IngestionCandidate.builder()
                .sourcePath(resource.getFilename()) // Simplified
                .fileName(resource.getFilename())
                .content(content.toString())
                .metadata(documents.isEmpty() ? null : documents.get(0).getMetadata())
                .build();
    }
}