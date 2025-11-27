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
        logger.debug("Getting cached embedding for text length: {}", text != null ? text.length() : 0);
        
        BsonArray embedding = embeddingProvider.getEmbedding(text);
        
        logger.debug("Generated embedding with dimension: {}", embedding != null ? embedding.size() : 0);
        return embedding;
    }
}

