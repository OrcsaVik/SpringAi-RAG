package com.github.springAi.vector;

import com.github.springAi.domain.DocumentSegment;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface VectorStoreRepository {
    /**
     * Store a list of segments with their pre-computed embeddings.
     */
    void upsertBatch(List<DocumentSegment> segments, List<float[]> embeddings);

    /**
     * Finds the top K nearest neighbors to a query vector, with optional metadata filtering.
     *
     * @param queryEmbedding The vector to search for.
     * @param topK           The number of results to return.
     * @param filters        A map of metadata key-value pairs to apply as a WHERE clause. Can be null or empty.
     * @return A list of matching DocumentSegments.
     */
    List<DocumentSegment> search(float[] queryEmbedding, int topK, Map<String, Object> filters);
    /**
     * Delete vectors for a specific talk (cleanup).
     */
    void deleteByTalkId(UUID talkId);
}