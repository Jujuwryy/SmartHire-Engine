package com.george.service;

import com.george.service.api.EmbeddingProvider;
import org.bson.BsonArray;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingCacheService {
    
    private final EmbeddingProvider embeddingProvider;

    public EmbeddingCacheService(EmbeddingProvider embeddingProvider) {
        this.embeddingProvider = embeddingProvider;
    }

    @Cacheable(value = "embeddings", key = "#text")
    public BsonArray getCachedEmbedding(String text) {
        return embeddingProvider.getEmbedding(text);
    }
}

