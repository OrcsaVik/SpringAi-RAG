package com.github.springAi.domain;

import lombok.Data;

import java.util.Map;

@Data
public class RagRequest {
    private String question;
    private int topK = 4; // Default to retrieving top 4 documents
    // Optional: for metadata filtering, e.g., {"source_file": "report-2023.pdf"}
    private Map<String, Object> filters;
}