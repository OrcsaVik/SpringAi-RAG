package com.github.springAi.embe;

import java.util.List;

public interface EmbeddingService {

    /**
     * Converts a list of text strings into a list of embedding vectors.
     * Implementations are expected to handle batching internally.
     *
     * @param texts A list of strings to embed.
     * @return A list of embedding vectors, where each vector is a List of Doubles.
     */
    List<List<Double>> embed(List<String> texts);

    /**
     * Returns the expected dimension of the embedding vectors produced by this service.
     *
     * @return The vector dimension.
     */
    int getDimensions();
}