package com.github.springAi.domain;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Represents the original source document's metadata.
 * Maps to the 'documents' table.
 */
@Data
@Builder
public class DocumentRecord {
    /**
     * Unique ID for the source document.
     */
    private UUID id;

    /**
     * The original path or URL of the file.
     */
    private String sourcePath;

    /**
     * SHA256 hash of the full content for idempotency checks.
     */
    private String sha256;

    /**
     * Flexible metadata about the source document (e.g., author, file_size).
     * Stored as JSONB in the database.
     */
    private Map<String, Object> metadata;

    /**
     * Timestamp of when the document was first ingested.
     */
    private OffsetDateTime createdAt;
}