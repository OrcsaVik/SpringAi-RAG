package com.github.springAi.Retriever;


import com.github.springAi.domain.DocumentSegment;
import com.github.springAi.vector.VectorStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class VectorStoreRetriever {

    private final VectorStoreRepository vectorStore;
    private final EmbeddingClient embeddingClient; // From Spring AI

    public List<DocumentSegment> retrieve(String query, int topK, Map<String, Object> filters) {
        // 1. Convert the user's query text into a vector
        List<Double> queryEmbeddingDouble = embeddingClient.embed(query);
        float[] queryEmbedding = toFloatArray(queryEmbeddingDouble);

        // 2. Search the vector store
        // NOTE: This assumes your VectorStoreRepository.search method is updated
        // to accept a generic filters map.
        // For now, we'll pass null if the filters are not yet implemented.
        // Let's assume user_id is a mandatory filter for this example.
        String userId = "default-user"; // Or get from security context
        
        // You would need to update the VectorStoreRepository search method signature to support generic filters
        return vectorStore.search(null, userId, queryEmbedding, topK);
    }
    
    private float[] toFloatArray(List<Double> in) {
        float[] out = new float[in.size()];
        for (int i = 0; i < in.size(); i++) {
            out[i] = in.get(i).floatValue();
        }
        return out;
    }
}