package com.george.service;

import com.george.service.api.EmbeddingProvider;
import io.micrometer.core.annotation.Timed;
import org.bson.BsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingCacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmbeddingCacheService.class);
    
    private final EmbeddingProvider embeddingProvider;

    public EmbeddingCacheService(EmbeddingProvider embeddingProvider) {
        this.embeddingProvider = embeddingProvider;
    }

    @Cacheable(value = "embeddings", key = "#text")
    @Timed(value = "embeddings.cache.operation", description = "Time taken for embedding cache operations")
    public BsonArray getCachedEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }
        if (embeddingProvider == null) {
            throw new IllegalStateException("EmbeddingProvider is not available");
        }
        
        logger.debug("Getting cached embedding for text length: {}", text.length());
        
        BsonArray embedding = embeddingProvider.getEmbedding(text);
        
        if (embedding == null) {
            throw new IllegalStateException("Failed to generate embedding - received null result");
        }
        
        logger.debug("Generated embedding with dimension: {}", embedding.size());
        return embedding;
    }
}

