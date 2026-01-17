package com.github.springAi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class SourceDocument {
    private UUID documentId;
    private UUID segmentId;
    private String content;
    private Map<String, Object> metadata;
}