package com.github.springAi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "rag-service")
public class RagServiceProperties {

    /**
     * The chat model to use for generating answers in the RAG pipeline.
     */
    private String chatModel = "gpt-4o-mini";

    /**
     * The temperature for the chat model. Lower values are more deterministic.
     */
    private double temperature = 0.2;

    /**
     * The token budget for the context window to avoid exceeding model limits.
     * This is a simple character count; a real tokenizer would be more precise.
     */
    private int contextTokenBudget = 4000;
}