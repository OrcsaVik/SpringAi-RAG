package com.github.springAi.embe;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@ConditionalOnProperty(name = "embedding-service.provider", havingValue = "mock", matchIfMissing = true)
public class LocalMockEmbeddingService implements EmbeddingService {

    private final EmbeddingServiceProperties properties;
    private final MessageDigest digest;

    public LocalMockEmbeddingService(EmbeddingServiceProperties properties) {
        this.properties = properties;
        try {
            this.digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        log.info("Initialized Local Mock Embedding Service with dimension: {}", properties.getDimensions());
    }

    @Override
    public List<List<Double>> embed(List<String> texts) {
        log.debug("Generating mock embeddings for {} texts", texts.size());
        return texts.stream().map(this::embed).collect(Collectors.toList());
    }
    
    private List<Double> embed(String text) {
        // Create a deterministic embedding based on the hash of the text
        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        long seed = bytesToLong(hash);
        Random random = new Random(seed);

        // Generate a normalized vector of the configured dimension
        return IntStream.range(0, getDimensions())
                .mapToDouble(i -> random.nextDouble() * 2 - 1) // Range [-1.0, 1.0]
                .boxed()
                .collect(Collectors.toList());
    }

    @Override
    public int getDimensions() {
        return properties.getDimensions();
    }

    private static long bytesToLong(byte[] bytes) {
        long value = 0;
        for (int i = 0; i < Math.min(bytes.length, 8); i++) {
            value = (value << 8) + (bytes[i] & 0xff);
        }
        return value;
    }
}