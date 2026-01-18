package com.github.springAi.vector;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.springAi.domain.DocumentSegment;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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

    /**
     * TODO wait to fill bec the field change
     * @param segments
     * @param embeddings
     */
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
                        .addValue("id", s.getId())
                        .addValue("documentId", s.getDocumentId())
                        .addValue("content", s.getContent())
                        .addValue("metadata", mapper.writeValueAsString(s.getMetadata()))
                        .addValue("embeddingStr", Arrays.toString(emb)) // Format: "[0.1, 0.2, ...]"
                        .addValue("createdAt", OffsetDateTime.now());
                batchParams.add(params);
            } catch (JsonProcessingException e) {
                log.error("Error serializing metadata for segment {}", s.getId(), e);
            }
        }

        jdbc.batchUpdate(sql, batchParams.toArray(new MapSqlParameterSource[0]));
        log.info("Upserted batch of {} vectors to Postgres", segments.size());
    }

    @SuppressWarnings("unchecked") // Suppress the warning for JsonParseException if it's not explicitly caught
    public List<DocumentSegment> search(float[] queryEmbedding, int topK, Map<String, Object> filters){
        // Base SQL query for vector similarity search
        StringBuilder sqlBuilder = new StringBuilder("""
        SELECT id, document_id, content, metadata,
               1 - (embedding <=> cast(:queryVector as vector)) as score
        FROM vector_segments
    """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("queryVector", Arrays.toString(queryEmbedding))
                .addValue("topK", topK);

        // --- DYNAMIC FILTERING LOGIC ---
        if (filters != null && !filters.isEmpty()) {
            sqlBuilder.append(" WHERE ");
            List<String> whereClauses = new ArrayList<>();
            filters.forEach((key, value) -> {
                // Use ->> operator to query JSONB text fields.
                // This requires a GIN index on metadata for performance.
                whereClauses.add("metadata->>:param_" + key + " = :value_" + key);
                params.addValue("param_" + key, key);
                params.addValue("value_" + key, String.valueOf(value)); // Cast value to string for '->>'
            });
            sqlBuilder.append(String.join(" AND ", whereClauses));
        }

        // Add ordering and limit
        sqlBuilder.append(" ORDER BY embedding <=> cast(:queryVector as vector) ASC LIMIT :topK");

        String finalSql = sqlBuilder.toString();
        log.debug("Executing vector search query: {}", finalSql);

        return jdbc.query(finalSql, params, (rs, rowNum) -> {
            try {
                return new DocumentSegment(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("documentId")),
                        mapper.readValue(rs.getString("metadata").getBytes(), Map.class),
                        rs.getString("content")

                );
            } catch (JsonProcessingException e) {
                throw new SQLException("Failed to parse metadata JSON", e);
            } catch (IOException e) {
                throw new JsonParseException("JSON PARSE appear wrong");
            }
        });
    }

    @Override
    public void deleteByTalkId(UUID talkId) {
        String sql = "DELETE FROM vectors WHERE talk_id = :talkId";
        jdbc.update(sql, new MapSqlParameterSource("talkId", talkId));
    }
}