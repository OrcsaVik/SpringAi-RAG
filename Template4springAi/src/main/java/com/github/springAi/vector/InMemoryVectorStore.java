package com.github.springAi.vector;


import com.github.springAi.domain.DocumentSegment;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
@Profile("test") // Active only during tests
public class InMemoryVectorStore implements VectorStoreRepository {

    // Key: Segment ID, Value: Record
    private final Map<UUID, InMemoryRecord> store = new ConcurrentHashMap<>();

    record InMemoryRecord(DocumentSegment segment, float[] embedding) {}

    @Override
    public void upsertBatch(List<DocumentSegment> segments, List<float[]> embeddings) {
        for (int i = 0; i < segments.size(); i++) {
            store.put(segments.get(i).id(), new InMemoryRecord(segments.get(i), embeddings.get(i)));
        }
    }

    @Override
    public List<DocumentSegment> search(UUID talkId, String userId, float[] query, int topK) {
        return store.values().stream()
                // 1. Filter by metadata
                .filter(r -> r.segment.talkId().equals(talkId) && r.segment.userId().equals(userId))
                // 2. Calculate Similarity (Cosine)
                .map(r -> new AbstractMap.SimpleEntry<>(r.segment, cosineSimilarity(query, r.embedding)))
                // 3. Sort descending (highest score first)
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                // 4. Limit
                .limit(topK)
                .map(AbstractMap.SimpleEntry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByTalkId(UUID talkId) {
        store.values().removeIf(r -> r.segment.talkId().equals(talkId));
    }

    // Your original math logic, moved here where it's appropriate for in-memory
    private double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) return 0.0;
        double dot = 0.0, na = 0.0, nb = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        return (na == 0 || nb == 0) ? 0.0 : dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
}