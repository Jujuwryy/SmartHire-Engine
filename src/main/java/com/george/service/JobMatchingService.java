package com.george.service;

import com.george.config.AppProperties;
import com.george.dto.JobMatchRequest;
import com.george.exception.ErrorCode;
import com.george.exception.JobMatchingException;
import com.george.model.JobMatch;
import com.george.util.Constants;
import com.george.util.DocumentMapper;
import com.george.util.MatchReasonGenerator;
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
    private final EmbeddingCacheService embeddingCacheService;
    private final AppProperties appProperties;
    private final MatchReasonGenerator matchReasonGenerator;
    private final DocumentMapper documentMapper;
    private final MatchingParameterNormalizer parameterNormalizer;
    private final VectorSearchQueryBuilder queryBuilder;

    public JobMatchingService(MongoClient mongoClient,
                              EmbeddingCacheService embeddingCacheService,
                              AppProperties appProperties,
                              MatchReasonGenerator matchReasonGenerator,
                              DocumentMapper documentMapper,
                              MatchingParameterNormalizer parameterNormalizer,
                              VectorSearchQueryBuilder queryBuilder) {
        this.mongoClient = mongoClient;
        this.embeddingCacheService = embeddingCacheService;
        this.appProperties = appProperties;
        this.matchReasonGenerator = matchReasonGenerator;
        this.documentMapper = documentMapper;
        this.parameterNormalizer = parameterNormalizer;
        this.queryBuilder = queryBuilder;
    }

    public List<JobMatch> findMatchingJobs(String userProfile) {
        if (userProfile == null || userProfile.trim().isEmpty()) {
            throw new IllegalArgumentException("User profile cannot be null or empty");
        }
        return findMatchingJobs(userProfile, 
            appProperties.getMatching().getDefaultLimit(), 
            appProperties.getMatching().getDefaultMinConfidence());
    }

    public List<JobMatch> findMatchingJobs(JobMatchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("JobMatchRequest cannot be null");
        }
        if (request.getUserProfile() == null || request.getUserProfile().trim().isEmpty()) {
            throw new IllegalArgumentException("User profile cannot be null or empty");
        }
        return findMatchingJobs(
            request.getUserProfile(),
            request.getLimit(),
            request.getMinConfidence()
        );
    }

    public List<JobMatch> findMatchingJobsSimple(String userProfile) {
        if (userProfile == null || userProfile.trim().isEmpty()) {
            throw new IllegalArgumentException("User profile cannot be null or empty");
        }
        if (userProfile.length() > Constants.MAX_USER_PROFILE_LENGTH) {
            throw new IllegalArgumentException("User profile cannot exceed " + Constants.MAX_USER_PROFILE_LENGTH + " characters");
        }
        return findMatchingJobs(userProfile, 
            appProperties.getMatching().getDefaultLimit(), 
            appProperties.getMatching().getDefaultMinConfidence());
    }

    @Timed(value = "job.matching.duration", description = "Time taken to find matching jobs")
    public List<JobMatch> findMatchingJobs(String userProfile, Integer limit, Double minConfidence) {
        if (userProfile == null || userProfile.trim().isEmpty()) {
            throw new IllegalArgumentException("User profile cannot be null or empty");
        }

        String processedProfile = parameterNormalizer.normalizeUserProfile(userProfile);
        int normalizedLimit = parameterNormalizer.normalizeLimit(limit);
        double normalizedMinConfidence = parameterNormalizer.normalizeMinConfidence(minConfidence);

        BsonArray userEmbedding = embeddingCacheService.getCachedEmbedding(processedProfile);
        if (userEmbedding == null) {
            throw new JobMatchingException(ErrorCode.JOB_MATCHING_EMBEDDING_ERROR, "Failed to generate embedding for user profile");
        }

        try {
            // Database and collection names are validated at startup by ConfigurationValidator
            String databaseName = appProperties.getMongodb().getDatabaseName();
            String collectionName = appProperties.getMongodb().getCollectionName();
            
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            if (database == null) {
                throw new JobMatchingException(ErrorCode.JOB_MATCHING_DATABASE_ERROR, "Failed to access MongoDB database: " + databaseName);
            }
            
            MongoCollection<Document> collection = database.getCollection(collectionName);
            if (collection == null) {
                throw new JobMatchingException(ErrorCode.JOB_MATCHING_DATABASE_ERROR, "Failed to access MongoDB collection: " + collectionName);
            }

            List<Document> pipeline = queryBuilder.buildSearchPipeline(userEmbedding, normalizedLimit, normalizedMinConfidence);
            if (pipeline == null || pipeline.isEmpty()) {
                throw new JobMatchingException(ErrorCode.JOB_MATCHING_FAILED, "Failed to build search pipeline");
            }

            List<JobMatch> matches = new ArrayList<>();
            final String finalProcessedProfile = processedProfile;
            collection.aggregate(pipeline)
                .forEach(doc -> {
                    if (doc == null) {
                        logger.warn("Received null document from MongoDB aggregation");
                        return;
                    }
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
        } catch (IllegalArgumentException | IllegalStateException | JobMatchingException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new JobMatchingException(ErrorCode.JOB_MATCHING_FAILED, "Failed to perform job matching", e);
        }
    }
}
