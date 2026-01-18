package com.github.springAi.config;

import com.github.springAi.embe.EmbeddingServiceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AiClientConfig {

    private final RagServiceProperties ragServiceProperties;
    private final EmbeddingServiceProperties embeddingServiceProperties;

    /**
     * Creates the low-level OpenAiApi client.
     * This is the foundational bean that communicates directly with the OpenAI REST API.
     * It's configured directly with the API key.
     *
     * @return A configured OpenAiApi instance.
     */
    @Bean
    public OpenAiApi openAiApi() {
        return new OpenAiApi(System.getenv("OPENAI_API_KEY"));
    }

    /**
     * TODO not find the targetClass
     * Creates the OpenAiEmbeddingClient bean using the manual OpenAiApi.
     * This follows the same pattern for consistency.
     *
     * @param openAiApi The foundational API client.
     * @return A configured OpenAiEmbeddingClient.
     */
    @Bean
    @ConditionalOnProperty(name = "embedding-service.provider", havingValue = "openai")
    public OpenAiEmbeddingClient openAiEmbeddingClient(OpenAiApi openAiApi) {
        // We can also add default options here if needed
        return new OpenAiEmbeddingClient(openAiApi);
    }

    /**
     * Creates the ChatModel bean.
     *
     * This is the direct implementation of the pattern from the Spring AI documentation.
     * We manually construct the OpenAiChatModel using our OpenAiApi bean and a set of
     * default options derived from our application properties.
     *
     * @param openAiApi The foundational API client.
     * @return A ChatModel bean ready for injection.
     */
    @Bean
    public ChatModel openAiChatModel(OpenAiApi openAiApi) {
        // Define the default options for our chat model
        OpenAiChatOptions defaultOptions = OpenAiChatOptions.builder()
                .withModel(ragServiceProperties.getChatModel())
                .withTemperature(ragServiceProperties.getTemperature())
                .build();

        // Create the ChatModel instance
        return new OpenAiChatModel(openAiApi, defaultOptions);
    }
}