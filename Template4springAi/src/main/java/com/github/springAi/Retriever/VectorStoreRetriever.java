package com.github.springAi.Retriever;


import com.github.springAi.domain.DocumentSegment;
import com.github.springAi.embe.EmbeddingService;
import com.github.springAi.vector.VectorStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class VectorStoreRetriever {

    private final VectorStoreRepository vectorStore;
    private final EmbeddingService embeddingService; // From Spring AI

    /**
     * Retrieves the most relevant document segments for a given query.
     *
     * @param query The user's natural language question.
     * @param topK The number of top segments to retrieve.
     * @param filters A map of metadata keys and values to filter the search results.
     *                For example: {"owner_user_id": "user-123"}
     * @return A list of DocumentSegments that are most semantically similar to the query.
     */
    public List<DocumentSegment> retrieve(String query, int topK, Map<String, Object> filters) {
        log.info("Retrieving documents for query: '{}' with topK: {} and filters: {}", query, topK, filters);

        // --- CORRECTED LOGIC ---
        // 1. Embed the user's query.
        // The embed service is designed for batching, so we wrap the single query in a list.
        List<List<Double>> embeddings = embeddingService.embed(List.of(query));

        if (embeddings.isEmpty()) {
            log.warn("Embedding for query resulted in no vectors. Query: '{}'", query);
            return List.of();
        }
        // Extract the first (and only) embedding vector from the result.
        float[] queryEmbedding = toFloatArray(embeddings.get(0));

        // 2. Search the vector store with the query vector AND the filters.
        // This requires the VectorStoreRepository to support dynamic filtering.
        return vectorStore.search(queryEmbedding, topK, filters);
    }
    private float[] toFloatArray(List<Double> in) {
        float[] out = new float[in.size()];
        for (int i = 0; i < in.size(); i++) {
            out[i] = in.get(i).floatValue();
        }
        return out;
    }
}