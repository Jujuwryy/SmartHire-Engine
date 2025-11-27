package com.george.service;

import com.george.config.AppProperties;
import com.george.dto.JobMatchRequest;
import com.george.exception.JobMatchingException;
import com.george.model.JobMatch;
import com.george.util.DocumentMapper;
import com.george.util.MatchReasonGenerator;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.micrometer.core.annotation.Timed;
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
        String processedProfile = parameterNormalizer.normalizeUserProfile(userProfile);
        int normalizedLimit = parameterNormalizer.normalizeLimit(limit);
        double normalizedMinConfidence = parameterNormalizer.normalizeMinConfidence(minConfidence);

        var userEmbedding = embeddingCacheService.getCachedEmbedding(processedProfile);

        try {
            String databaseName = appProperties.getMongodb().getDatabaseName();
            String collectionName = appProperties.getMongodb().getCollectionName();
            
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(collectionName);

            List<Document> pipeline = queryBuilder.buildSearchPipeline(userEmbedding, normalizedLimit, normalizedMinConfidence);

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
