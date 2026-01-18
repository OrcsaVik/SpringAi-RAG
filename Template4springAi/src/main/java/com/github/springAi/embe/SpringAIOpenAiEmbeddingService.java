package com.github.springAi.embe;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(name = "embedding-service.provider", havingValue = "openai")
public class SpringAIOpenAiEmbeddingService implements EmbeddingService {

    private final EmbeddingModel embeddingClient;
    private final EmbeddingServiceProperties properties;

    public SpringAIOpenAiEmbeddingService(EmbeddingModel embeddingClient, EmbeddingServiceProperties properties) {
        this.embeddingClient = embeddingClient;
        this.properties = properties;
        log.info("Initialized OpenAI Embedding Service with model: {}", properties.getOpenai().getEmbeddingModel());
    }

    @Override
    public List<List<Double>> embed(List<String> texts) {
        if (CollectionUtils.isEmpty(texts)) {
            return new ArrayList<>();
        }

        List<List<Double>> allEmbeddings = new ArrayList<>();
        int batchSize = properties.getBatchSize();
        
        // Process the texts in batches
        for (int i = 0; i < texts.size(); i += batchSize) {
            int end = Math.min(i + batchSize, texts.size());
            List<String> batch = texts.subList(i, end);

            log.debug("Embedding batch of {} texts (from index {} to {})", batch.size(), i, end -1);
            
            // Use Spring AI options to specify the model at runtime
            EmbeddingRequest request = new EmbeddingRequest(batch,
                    OpenAiEmbeddingOptions.builder()
                            .withModel(properties.getOpenai().getEmbeddingModel())
                            .build());

            allEmbeddings.addAll(embeddingClient.call(request).getResults().stream()
                    .map(e -> e.getOutput())
                    .toList());
        }

        return allEmbeddings;
    }

    @Override
    public int getDimensions() {
        return properties.getDimensions();
    }
}