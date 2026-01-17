-- 1. Enable the pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. Create the vectors table
-- Note: We replaced 'embedding_json' with the native 'vector' type.
-- We keep talk_id and user_id as explicit columns for fast filtering,
-- but also allow generic metadata.
CREATE TABLE IF NOT EXISTS vectors (
                                       id UUID PRIMARY KEY,
                                       talk_id UUID NOT NULL,
                                       user_id VARCHAR(255) NOT NULL,
    content TEXT,
    metadata JSONB,
    embedding vector(1536), -- Adjust 1536 to your model (e.g., 768 for Bert, 1536 for OpenAI)
    created_at TIMESTAMPTZ DEFAULT now()
    );

-- 3. Create an HNSW or IVFFlat index for performance
-- This allows approximate nearest neighbor search (ANN) instead of full table scan
CREATE INDEX IF NOT EXISTS vectors_embedding_idx
    ON vectors
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

-- 4. Create index on filter columns
CREATE INDEX IF NOT EXISTS idx_vectors_talk_user ON vectors(talk_id, user_id);

-- 1. Ensure the pgvector extension is enabled
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. Refined 'documents' table
CREATE TABLE IF NOT EXISTS documents (
                                         id UUID PRIMARY KEY,
                                         user_id TEXT NOT NULL,           -- Multi-tenant owner
                                         source_path TEXT NOT NULL,
                                         sha256 TEXT NOT NULL,            -- Hash for idempotency
                                         metadata JSONB DEFAULT '{}',     -- File-level metadata
                                         created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
    -- Idempotency check: unique per user to allow different users to upload same file
    UNIQUE(user_id, sha256)
    );

-- 3. Phase C Optimized 'vector_segments' table
CREATE TABLE IF NOT EXISTS vector_segments (
                                               id UUID PRIMARY KEY,
                                               document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    talk_id UUID NOT NULL,           -- Session-aware link
    user_id TEXT NOT NULL,           -- For fast pre-filtering
    content TEXT NOT NULL,
    metadata JSONB DEFAULT '{}',     -- Chunk-specific info (page, sequence)
    embedding vector(1536) NOT NULL, -- OpenAI dimension
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL
    );

---
--- INDEXING STRATEGY FOR PHASE C ---
---

-- A. HNSW Vector Index (Faster & More Accurate than IVFFlat)
-- We use 'vector_cosine_ops' for OpenAI embeddings.
CREATE INDEX IF NOT EXISTS idx_vector_segments_embedding_hnsw
    ON vector_segments USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

-- B. Metadata GIN Index (For searching inside JSONB fields)
CREATE INDEX IF NOT EXISTS idx_vector_segments_metadata_gin
    ON vector_segments USING GIN (metadata);

-- C. Composite Index for Multi-tenancy (Critical for 200/s burst)
-- This allows the DB to instantly narrow down segments to a specific user/session
CREATE INDEX IF NOT EXISTS idx_segments_user_talk
    ON vector_segments (user_id, talk_id);ent in the documents table.';