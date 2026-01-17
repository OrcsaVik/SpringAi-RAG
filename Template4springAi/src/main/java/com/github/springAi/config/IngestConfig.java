package com.github.springAi.config;

import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IngestConfig {

    // Task: Chunking Configuration
    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        // Default: 800 tokens, 350 overlap (adjustable)
        return new TokenTextSplitter(800, 350, 5, 10000, true);
    }
}