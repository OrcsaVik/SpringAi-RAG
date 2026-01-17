package com.github.springAi.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.ai.document.Document;
import java.util.Map;

@Data
@Builder
public class IngestionCandidate {
    private String sourcePath;
    private String fileName;
    private String content; // The full text
    private String contentHash; // SHA256
    private Map<String, Object> metadata;

    public Document toDocument() {
        // Convert to Spring AI Document
        return new Document(content, metadata);
    }
}