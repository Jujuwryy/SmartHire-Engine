package com.george.service.api;

import org.bson.BsonArray;

import java.util.List;

public interface EmbeddingProvider {
    List<BsonArray> getEmbeddings(List<String> texts);
    BsonArray getEmbedding(String text);
}

