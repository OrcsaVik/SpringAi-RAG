package com.github.springAi.domain;

import java.util.Map;
import java.util.UUID;

/**
 * Represents a single chunk (segment) of a DocumentRecord.
 * This is the unit that gets embedded and stored for vector search.
 * Maps to the 'vector_segments' table.
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class DocumentSegment {


    @lombok.Builder.Default
    UUID id = UUID.randomUUID();
    UUID talkId;      // Session-aware: Links segment to a specific conversation
    String userId;    // Multi-tenant: Ensures data isolation per user
    String content;   // The text chunk for embedding
    Map<String, Object> metadata; // Includes page_num, chunk_seq, talkId, etc.
}