package com.george.service;

import com.george.Vector.VectorEmbeddings;
import com.george.config.AppProperties;
import com.george.dto.JobMatchRequest;
import com.george.exception.JobMatchingException;
import com.george.model.JobMatch;
import com.george.util.DocumentMapper;
import com.george.util.MatchReasonGenerator;
import com.george.util.TextPreprocessor;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.micrometer.core.annotation.Timed;
import org.bson.BsonArray;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobMatchingService {

    private static final Logger logger = LoggerFactory.getLogger(JobMatchingService.class);

    private final MongoClient mongoClient;
    private final VectorEmbeddings vectorEmbeddings;
    private final EmbeddingCacheService embeddingCacheService;
    private final AppProperties appProperties;
    private final MatchReasonGenerator matchReasonGenerator;
    private final DocumentMapper documentMapper;

    public JobMatchingService(MongoClient mongoClient,
                              VectorEmbeddings vectorEmbeddings,
                              EmbeddingCacheService embeddingCacheService,
                              AppProperties appProperties,
                              MatchReasonGenerator matchReasonGenerator,
                              DocumentMapper documentMapper) {
        this.mongoClient = mongoClient;
        this.vectorEmbeddings = vectorEmbeddings;
        this.embeddingCacheService = embeddingCacheService;
        this.appProperties = appProperties;
        this.matchReasonGenerator = matchReasonGenerator;
        this.documentMapper = documentMapper;
    }

    public List<JobMatch> findMatchingJobs(String userProfile) {
        return findMatchingJobs(userProfile, 
            appProperties.getMatching().getDefaultLimit(), 
            appProperties.getMatching().getDefaultMinConfidence());
    }

    public List<JobMatch> findMatchingJobs(JobMatchRequest request) {
        return findMatchingJobs(
            request.getUserProfile(),
            request.getLimit(),
            request.getMinConfidence()
        );
    }

    @Timed(value = "job.matching.duration", description = "Time taken to find matching jobs")
    public List<JobMatch> findMatchingJobs(String userProfile, Integer limit, Double minConfidence) {
        if (userProfile == null || userProfile.trim().isEmpty()) {
            throw new IllegalArgumentException("User profile cannot be null or empty");
        }
        
        String processedProfile = TextPreprocessor.preprocess(userProfile);
        processedProfile = TextPreprocessor.validateAndTruncate(processedProfile, 2000);
        
        int maxLimit = appProperties.getMatching().getMaxLimit();
        int defaultLimit = appProperties.getMatching().getDefaultLimit();
        double defaultMinConf = appProperties.getMatching().getDefaultMinConfidence();
        
        int normalizedLimit = limit != null && limit > 0 && limit <= maxLimit 
            ? limit : defaultLimit;
        double normalizedMinConfidence = minConfidence != null && minConfidence >= 0.0 && minConfidence <= 1.0
            ? minConfidence : defaultMinConf;

        BsonArray userEmbedding = embeddingCacheService.getCachedEmbedding(processedProfile);

        try {
            String databaseName = appProperties.getMongodb().getDatabaseName();
            String collectionName = appProperties.getMongodb().getCollectionName();
            String vectorIndexName = appProperties.getMongodb().getVectorIndexName();
            
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(collectionName);

            List<Document> pipeline = new ArrayList<>();
            
            Document searchStage = new Document("$search", new Document()
                .append("index", vectorIndexName)
                .append("knnBeta", new Document()
                    .append("vector", userEmbedding)
                    .append("path", "embedding")
                    .append("k", normalizedLimit * 2)));
            
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
            
            if (normalizedMinConfidence > 0.0) {
                pipeline.add(new Document("$match", 
                    new Document("score", new Document("$gte", normalizedMinConfidence))));
            }
            
            pipeline.add(new Document("$limit", normalizedLimit));

            List<JobMatch> matches = new ArrayList<>();
            final String finalProcessedProfile = processedProfile;
            collection.aggregate(pipeline)
                .forEach(doc -> {
                    try {
                        JobMatch match = new JobMatch();
                        match.setJob(documentMapper.toPost(doc));
                        Double score = doc.getDouble("score");
                        match.setConfidence(score != null ? score : 0.0);
                        match.setMatchReasons(matchReasonGenerator.generateMatchReasons(doc, finalProcessedProfile));
                        matches.add(match);
                    } catch (Exception e) {
                        logger.warn("Failed to process document in job matching: {}", e.getMessage());
                    }
                });

            return matches;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new JobMatchingException("Failed to perform job matching", e);
        }
    }
}
