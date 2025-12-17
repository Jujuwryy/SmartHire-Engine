package com.george.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates required configuration properties at application startup.
 * Fails fast if any required configuration is missing or invalid.
 */
@Component
public class ConfigurationValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationValidator.class);
    
    private final AppProperties appProperties;
    private final String mongoConnectionString;
    
    public ConfigurationValidator(
            AppProperties appProperties,
            @Value("${spring.data.mongodb.uri}") String mongoConnectionString) {
        this.appProperties = appProperties;
        this.mongoConnectionString = mongoConnectionString;
    }
    
    @PostConstruct
    public void validateConfiguration() {
        logger.info("Validating application configuration...");
        List<String> errors = new ArrayList<>();
        
        // Validate MongoDB configuration
        validateMongoConfiguration(errors);
        
        // Validate Embedding configuration
        validateEmbeddingConfiguration(errors);
        
        // Validate Matching configuration
        validateMatchingConfiguration(errors);
        
        // If any errors found, fail startup
        if (!errors.isEmpty()) {
            String errorMessage = "Configuration validation failed:\n" + String.join("\n", errors);
            logger.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        
        logger.info("Configuration validation passed successfully");
    }
    
    private void validateMongoConfiguration(List<String> errors) {
        // MongoDB connection string
        if (mongoConnectionString == null || mongoConnectionString.trim().isEmpty()) {
            errors.add("ERROR: ATLAS_CONNECTION_STRING (spring.data.mongodb.uri) is required but not set");
        } else if (mongoConnectionString.equals("mongodb://localhost:27017")) {
            logger.warn("WARNING: Using default MongoDB connection string (localhost:27017). " +
                       "Set ATLAS_CONNECTION_STRING for production.");
        }
        
        // Database name
        String databaseName = appProperties.getMongodb().getDatabaseName();
        if (databaseName == null || databaseName.trim().isEmpty()) {
            errors.add("ERROR: MONGO_DATABASE_NAME (app.mongodb.database-name) is required but not set");
        }
        
        // Collection name
        String collectionName = appProperties.getMongodb().getCollectionName();
        if (collectionName == null || collectionName.trim().isEmpty()) {
            errors.add("ERROR: MONGO_COLLECTION_NAME (app.mongodb.collection-name) is required but not set");
        }
        
        // Vector index name
        String vectorIndexName = appProperties.getMongodb().getVectorIndexName();
        if (vectorIndexName == null || vectorIndexName.trim().isEmpty()) {
            errors.add("ERROR: MONGO_VECTOR_INDEX_NAME (app.mongodb.vector-index-name) is required but not set");
        }
    }
    
    private void validateEmbeddingConfiguration(List<String> errors) {
        AppProperties.Embeddings.Huggingface huggingface = appProperties.getEmbeddings().getHuggingface();
        
        // Access token
        String accessToken = huggingface.getAccessToken();
        if (accessToken == null || accessToken.trim().isEmpty()) {
            errors.add("ERROR: HUGGING_FACE_ACCESS_TOKEN (app.embeddings.huggingface.access-token) is required but not set");
        }
        
        // Model ID
        String modelId = huggingface.getModelId();
        if (modelId == null || modelId.trim().isEmpty()) {
            errors.add("ERROR: EMBEDDING_MODEL_ID (app.embeddings.huggingface.model-id) is required but not set");
        }
        
        // Timeout validation
        int timeoutSeconds = huggingface.getTimeoutSeconds();
        if (timeoutSeconds <= 0) {
            errors.add("ERROR: EMBEDDING_TIMEOUT_SECONDS (app.embeddings.huggingface.timeout-seconds) must be greater than 0");
        }
        
        // Dimension validation
        int dimension = huggingface.getDimension();
        if (dimension <= 0) {
            errors.add("ERROR: EMBEDDING_DIMENSION (app.embeddings.huggingface.dimension) must be greater than 0");
        }
    }
    
    private void validateMatchingConfiguration(List<String> errors) {
        AppProperties.Matching matching = appProperties.getMatching();
        
        // Default limit validation
        int defaultLimit = matching.getDefaultLimit();
        if (defaultLimit <= 0) {
            errors.add("ERROR: MATCH_DEFAULT_LIMIT (app.matching.default-limit) must be greater than 0");
        }
        
        // Max limit validation
        int maxLimit = matching.getMaxLimit();
        if (maxLimit <= 0) {
            errors.add("ERROR: MATCH_MAX_LIMIT (app.matching.max-limit) must be greater than 0");
        }
        
        // Min limit validation
        int minLimit = matching.getMinLimit();
        if (minLimit <= 0) {
            errors.add("ERROR: MATCH_MIN_LIMIT (app.matching.min-limit) must be greater than 0");
        }
        
        // Limit range validation
        if (minLimit > defaultLimit || defaultLimit > maxLimit) {
            errors.add("ERROR: MATCH limits must satisfy: min-limit <= default-limit <= max-limit");
        }
        
        // Confidence validation
        double defaultMinConfidence = matching.getDefaultMinConfidence();
        if (defaultMinConfidence < 0.0 || defaultMinConfidence > 1.0) {
            errors.add("ERROR: MATCH_MIN_CONFIDENCE (app.matching.default-min-confidence) must be between 0.0 and 1.0");
        }
        
        // Threshold validation
        AppProperties.Matching.Thresholds thresholds = matching.getThresholds();
        if (thresholds.getVeryStrong() < 0.0 || thresholds.getVeryStrong() > 1.0 ||
            thresholds.getGood() < 0.0 || thresholds.getGood() > 1.0 ||
            thresholds.getModerate() < 0.0 || thresholds.getModerate() > 1.0) {
            errors.add("ERROR: MATCH thresholds must be between 0.0 and 1.0");
        }
        
        // Threshold ordering validation
        if (thresholds.getModerate() >= thresholds.getGood() || 
            thresholds.getGood() >= thresholds.getVeryStrong()) {
            errors.add("ERROR: MATCH thresholds must satisfy: moderate < good < very-strong");
        }
    }
}

