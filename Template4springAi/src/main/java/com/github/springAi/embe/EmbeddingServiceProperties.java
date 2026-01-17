package com.github.springAi.embe;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "embedding-service")
public class EmbeddingServiceProperties {

    /**
     * The active embedding provider. Can be "openai" or "mock".
     */
    private String provider = "openai";

    /**
     * Default batch size for embedding requests to avoid overloading the provider.
     */
    private int batchSize = 100;

    /**
     * The output dimension of the embedding model.
     * OpenAI text-embedding-3-small is 1536.
     * OpenAI text-embedding-3-large is 3072.
     */
    private int dimensions = 1536;

    /**
     * Configuration specific to the OpenAI provider.
     */
    private OpenAiProperties openai = new OpenAiProperties();

    @Data
    public static class OpenAiProperties {
        /**
         * The specific OpenAI model to use for embeddings.
         */
        private String embeddingModel = "text-embedding-3-small";
    }
}