package com.github.springAi.vector;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.springAi.domain.DocumentSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Repository
@Profile("!test") // Use this for prod/dev, but not unit tests
@RequiredArgsConstructor
public class PostgresVectorStore implements VectorStoreRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper mapper;

    @Override
    @Transactional
    public void upsertBatch(List<DocumentSegment> segments, List<float[]> embeddings) {
        if (segments.size() != embeddings.size()) {
            throw new IllegalArgumentException("Segments and embeddings size mismatch");
        }

        // Native Postgres Vector Syntax: cast(:embeddingStr as vector)
        String sql = """
            INSERT INTO vectors (id, talk_id, user_id, content, metadata, embedding, created_at)
            VALUES (:id, :talkId, :userId, :content, cast(:metadata as jsonb), cast(:embeddingStr as vector), :createdAt)
            ON CONFLICT (id) 
            DO UPDATE SET 
                content = EXCLUDED.content,
                metadata = EXCLUDED.metadata,
                embedding = EXCLUDED.embedding,
                created_at = EXCLUDED.created_at
            """;

        List<MapSqlParameterSource> batchParams = new ArrayList<>();

        for (int i = 0; i < segments.size(); i++) {
            DocumentSegment s = segments.get(i);
            float[] emb = embeddings.get(i);

            try {
                MapSqlParameterSource params = new MapSqlParameterSource()
                        .addValue("id", s.id())
                        .addValue("talkId", s.talkId())
                        .addValue("userId", s.userId())
                        .addValue("content", s.content())
                        .addValue("metadata", mapper.writeValueAsString(s.metadata()))
                        .addValue("embeddingStr", Arrays.toString(emb)) // Format: "[0.1, 0.2, ...]"
                        .addValue("createdAt", OffsetDateTime.now());
                batchParams.add(params);
            } catch (JsonProcessingException e) {
                log.error("Error serializing metadata for segment {}", s.id(), e);
            }
        }

        jdbc.batchUpdate(sql, batchParams.toArray(new MapSqlParameterSource[0]));
        log.info("Upserted batch of {} vectors to Postgres", segments.size());
    }

    @Override
    public List<DocumentSegment> search(UUID talkId, String userId, float[] query, int topK) {
        // Native Vector Search using Cosine Distance (<=>)
        // 1 - (vector <=> query) gives the similarity score (where 1 is identical)
        String sql = """
            SELECT id, talk_id, user_id, content, metadata, 
                   1 - (embedding <=> cast(:queryVector as vector)) as score
            FROM vectors
            WHERE talk_id = :talkId 
              AND user_id = :userId
            ORDER BY embedding <=> cast(:queryVector as vector) ASC
            LIMIT :topK
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("talkId", talkId)
                .addValue("userId", userId)
                .addValue("queryVector", Arrays.toString(query))
                .addValue("topK", topK);

        return jdbc.query(sql, params, (rs, rowNum) -> {
            try {
                return new DocumentSegment(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("talk_id")),
                        rs.getString("user_id"),
                        rs.getString("content"),
                        mapper.readValue(rs.getString("metadata"), Map.class)
                );
            } catch (JsonProcessingException e) {
                throw new SQLException("Failed to parse metadata JSON", e);
            }
        });
    }

    @Override
    public void deleteByTalkId(UUID talkId) {
        String sql = "DELETE FROM vectors WHERE talk_id = :talkId";
        jdbc.update(sql, new MapSqlParameterSource("talkId", talkId));
    }
}