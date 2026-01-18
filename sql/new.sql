-- Enable the pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- 1. Create the parent 'documents' table.
-- This table stores metadata about the original source files.
CREATE TABLE IF NOT EXISTS documents (
                                         id UUID PRIMARY KEY,
                                         source_path TEXT NOT NULL,
                                         sha256 TEXT NOT NULL UNIQUE, -- Ensures we never ingest the same file twice
                                         metadata JSONB,
                                         created_at TIMESTAMPTZ DEFAULT now() NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_documents_sha256 ON documents(sha256);
COMMENT ON TABLE documents IS 'Stores metadata for original source documents.';

-- 2. Create the 'vector_segments' table for chunks.
CREATE TABLE IF NOT EXISTS vector_segments (
                                               id UUID PRIMARY KEY,
    -- This foreign key is the link back to the original document.
    -- ON DELETE CASCADE means if you delete the parent document, all its chunks are also deleted.
                                               document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    content TEXT,
    metadata JSONB,
    -- The vector embedding. Adjust 1536 to your model's dimension.
    embedding vector(1536) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL
    );

-- Index on the foreign key for fast lookups.
CREATE INDEX IF NOT EXISTS idx_vector_segments_document_id ON vector_segments(document_id);

-- The vector index for fast similarity search.
CREATE INDEX IF NOT EXISTS vector_segments_embedding_idx
    ON vector_segments
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

-- A GIN index on the metadata column allows for fast filtering on JSONB keys.
-- This is how you'll efficiently query for segments where metadata contains '{"owner_user_id": "user-123"}'.
CREATE INDEX IF NOT EXISTS idx_vector_segments_metadata_gin ON vector_segments USING GIN (metadata);

COMMENT ON TABLE vector_segments IS 'Stores text chunks (segments) and their vector embeddings.';
COMMENT ON COLUMN vector_segments.document_id IS 'Foreign key to the parent document in the documents table.';