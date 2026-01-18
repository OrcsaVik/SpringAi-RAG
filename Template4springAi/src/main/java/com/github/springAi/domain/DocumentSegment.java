package com.github.springAi.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a single chunk (segment) of a DocumentRecord.
 * This is the unit that gets embedded and stored for vector search.
 * Maps to the 'vector_segments' table.
 * TOOO
 */
@Value
@AllArgsConstructor
@Builder(toBuilder = true)
public class DocumentSegment {

    UUID id;

    /**
     * Foreign key linking back to the parent DocumentRecord.
     * THIS IS THE CRITICAL FIELD that was missing.
     */
    UUID documentId;

    /**
     * The actual text content of this chunk, which will be embedded.
     */
    String content;

    /**
     * Metadata specific to this chunk (e.g., page_number, chunk_sequence).
     * This is also where you can add filterable tags like user_id or talk_id
     * if a segment needs to be scoped to a specific context.
     */
    Map<String, Object> metadata;



    @Builder.Default
    OffsetDateTime createdAt = OffsetDateTime.now();

    public DocumentSegment(UUID id ,UUID documentId, Map<String, Object> metadata,  String content) {
        this.id = id;
        this.metadata = metadata;
        this.documentId = documentId;
        this.content = content;
    }
}