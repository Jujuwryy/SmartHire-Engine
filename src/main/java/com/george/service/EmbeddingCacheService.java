package com.george.service;

import org.bson.BsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.george.Vector.VectorEmbeddings;

@Service
public class EmbeddingCacheService {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingCacheService.class);
    
    private final VectorEmbeddings vectorEmbeddings;

    public EmbeddingCacheService(VectorEmbeddings vectorEmbeddings) {
        this.vectorEmbeddings = vectorEmbeddings;
    }

    @Cacheable(value = "embeddings", key = "#text.hashCode()")
    public BsonArray getCachedEmbedding(String text) {
        return vectorEmbeddings.getEmbedding(text);
    }
}

