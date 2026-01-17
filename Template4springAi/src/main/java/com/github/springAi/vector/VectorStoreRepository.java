package com.github.springAi.vector;

import com.github.springAi.domain.DocumentSegment;

import java.util.List;
import java.util.UUID;

public interface VectorStoreRepository {
    /**
     * Store a list of segments with their pre-computed embeddings.
     */
    void upsertBatch(List<DocumentSegment> segments, List<float[]> embeddings);

    /**
     * Find nearest neighbors.
     * @param talkId Filter by talk context
     * @param queryEmbedding The vector to search for
     * @param topK Number of results to return
     */
    List<DocumentSegment> search(UUID talkId, String userId, float[] queryEmbedding, int topK);

    /**
     * Delete vectors for a specific talk (cleanup).
     */
    void deleteByTalkId(UUID talkId);
}