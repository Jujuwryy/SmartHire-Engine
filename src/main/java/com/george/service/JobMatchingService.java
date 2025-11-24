package com.george.service;

import com.george.Vector.VectorEmbeddings;
import com.george.config.AppProperties;
import com.george.dto.JobMatchRequest;
import com.george.exception.JobMatchingException;
import com.george.model.JobMatch;
import com.george.model.Post;
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

    public JobMatchingService(MongoClient mongoClient,
                              VectorEmbeddings vectorEmbeddings,
                              EmbeddingCacheService embeddingCacheService,
                              AppProperties appProperties,
                              MatchReasonGenerator matchReasonGenerator) {
        this.mongoClient = mongoClient;
        this.vectorEmbeddings = vectorEmbeddings;
        this.embeddingCacheService = embeddingCacheService;
        this.appProperties = appProperties;
        this.matchReasonGenerator = matchReasonGenerator;
    }

    /**
     * Finds matching jobs based on user profile using vector similarity search
     * 
     * @param userProfile Text description of user's skills and preferences
     * @return List of matching jobs with similarity scores
     */
    public List<JobMatch> findMatchingJobs(String userProfile) {
        return findMatchingJobs(userProfile, 
            appProperties.getMatching().getDefaultLimit(), 
            appProperties.getMatching().getDefaultMinConfidence());
    }

    /**
     * Finds matching jobs with advanced filtering options
     * 
     * @param request JobMatchRequest containing user profile and filtering options
     * @return List of matching jobs with similarity scores
     */
    public List<JobMatch> findMatchingJobs(JobMatchRequest request) {
        return findMatchingJobs(
            request.getUserProfile(),
            request.getLimit(),
            request.getMinConfidence()
        );
    }

    /**
     * Finds matching jobs based on user profile using vector similarity search
     * 
     * @param userProfile Text description of user's skills and preferences
     * @param limit Maximum number of results to return
     * @param minConfidence Minimum confidence score threshold
     * @return List of matching jobs with similarity scores
     */
    @Timed(value = "job.matching.duration", description = "Time taken to find matching jobs")
    public List<JobMatch> findMatchingJobs(String userProfile, Integer limit, Double minConfidence) {
        // Validate and preprocess inputs
        if (userProfile == null || userProfile.trim().isEmpty()) {
            throw new IllegalArgumentException("User profile cannot be null or empty");
        }
        
        // Preprocess user profile text
        String processedProfile = TextPreprocessor.preprocess(userProfile);
        processedProfile = TextPreprocessor.validateAndTruncate(processedProfile, 2000);
        
        // Normalize limit
        int maxLimit = appProperties.getMatching().getMaxLimit();
        int defaultLimit = appProperties.getMatching().getDefaultLimit();
        double defaultMinConf = appProperties.getMatching().getDefaultMinConfidence();
        
        int normalizedLimit = limit != null && limit > 0 && limit <= maxLimit 
            ? limit : defaultLimit;
        double normalizedMinConfidence = minConfidence != null && minConfidence >= 0.0 && minConfidence <= 1.0
            ? minConfidence : defaultMinConf;

        // Generate embedding for user profile (with caching)
        BsonArray userEmbedding = embeddingCacheService.getCachedEmbedding(processedProfile);

        try {
            String databaseName = appProperties.getMongodb().getDatabaseName();
            String collectionName = appProperties.getMongodb().getCollectionName();
            String vectorIndexName = appProperties.getMongodb().getVectorIndexName();
            
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(collectionName);

            // Create aggregation pipeline for vector search
            List<Document> pipeline = new ArrayList<>();
            
            // Vector search stage
            Document searchStage = new Document("$search", new Document()
                .append("index", vectorIndexName)
                .append("knnBeta", new Document()
                    .append("vector", userEmbedding)
                    .append("path", "embedding")
                    .append("k", normalizedLimit * 2))); // Get more results for filtering
            
            pipeline.add(searchStage);
            
            // Project stage
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
            
            // Match stage for confidence filtering
            if (normalizedMinConfidence > 0.0) {
                pipeline.add(new Document("$match", 
                    new Document("score", new Document("$gte", normalizedMinConfidence))));
            }
            
            // Limit stage
            pipeline.add(new Document("$limit", normalizedLimit));

            // Execute search and convert results
            List<JobMatch> matches = new ArrayList<>();
            final String finalProcessedProfile = processedProfile; // Make effectively final for lambda
            collection.aggregate(pipeline)
                .forEach(doc -> {
                    try {
                        JobMatch match = new JobMatch();
                        match.setJob(convertDocumentToPost(doc));
                        Double score = doc.getDouble("score");
                        match.setConfidence(score != null ? score : 0.0);
                        match.setMatchReasons(matchReasonGenerator.generateMatchReasons(doc, finalProcessedProfile));
                        matches.add(match);
                    } catch (Exception e) {
                        // Continue processing other documents - error already logged by AOP
                    }
                });

            return matches;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new JobMatchingException("Failed to perform job matching", e);
        }
    }

    private Post convertDocumentToPost(Document doc) {
        Post post = new Post();
        
        if (doc.getObjectId("_id") != null) {
            post.setId(doc.getObjectId("_id").toString());
        }
        post.setJobTitle(doc.getString("jobTitle"));
        post.setJobDescription(doc.getString("jobDescription"));
        post.setExperience(doc.getInteger("experience"));
        post.setRequiredTechs(doc.getList("requiredTechs", String.class));
        post.setCompany(doc.getString("company"));
        post.setLocation(doc.getString("location"));
        post.setEmploymentType(doc.getString("employmentType"));
        post.setSalaryMin(doc.getDouble("salaryMin"));
        post.setSalaryMax(doc.getDouble("salaryMax"));
        post.setCurrency(doc.getString("currency"));
        
        return post;
    }
}
