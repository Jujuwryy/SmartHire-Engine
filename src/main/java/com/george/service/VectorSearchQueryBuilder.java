package com.george.service;

import com.george.config.AppProperties;
import org.bson.BsonArray;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class VectorSearchQueryBuilder {
    
    private final AppProperties appProperties;
    
    public VectorSearchQueryBuilder(AppProperties appProperties) {
        this.appProperties = appProperties;
    }
    
    public List<Document> buildSearchPipeline(BsonArray userEmbedding, int limit, double minConfidence) {
        List<Document> pipeline = new ArrayList<>();
        
        String vectorIndexName = appProperties.getMongodb().getVectorIndexName();
        
        Document searchStage = new Document("$search", new Document()
            .append("index", vectorIndexName)
            .append("knnBeta", new Document()
                .append("vector", userEmbedding)
                .append("path", "embedding")
                .append("k", limit * 2)));
        
        pipeline.add(searchStage);
        
        Document projectStage = new Document("$project", new Document()
            .append("jobTitle", 1)
            .append("jobDescription", 1)
            .append("experience", 1)
            .append("requiredTechs", 1)
            .append("company", 1)
            .append("location", 1)
            .append("employmentType", 1)
            .append("salaryMin", 1)
            .append("salaryMax", 1)
            .append("currency", 1)
            .append("score", new Document("$meta", "searchScore")));
        
        pipeline.add(projectStage);
        
        if (minConfidence > 0.0) {
            pipeline.add(new Document("$match", 
                new Document("score", new Document("$gte", minConfidence))));
        }
        
        pipeline.add(new Document("$limit", limit));
        
        return pipeline;
    }
}

