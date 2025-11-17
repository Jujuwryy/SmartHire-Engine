package com.george.util;

public class Constants {
    
    // API Endpoints
    public static final String API_V1_BASE = "/api/v1";
    public static final String VECTORS_ENDPOINT = API_V1_BASE + "/vectors";
    public static final String JOBS_ENDPOINT = API_V1_BASE + "/jobs";
    
    // MongoDB Configuration
    public static final String DATABASE_NAME = "sample_db";
    public static final String COLLECTION_NAME = "JobPost";
    public static final String VECTOR_INDEX_NAME = "vector_index";
    
    // Embedding Configuration
    public static final String EMBEDDING_MODEL_ID = "mixedbread-ai/mxbai-embed-large-v1";
    public static final int EMBEDDING_TIMEOUT_SECONDS = 60;
    public static final int DEFAULT_EMBEDDING_DIMENSION = 1024;
    
    // Matching Configuration
    public static final int DEFAULT_MATCH_LIMIT = 10;
    public static final int MAX_MATCH_LIMIT = 100;
    public static final int MIN_MATCH_LIMIT = 1;
    public static final double DEFAULT_MIN_CONFIDENCE = 0.0;
    
    // Confidence Thresholds
    public static final double VERY_STRONG_MATCH_THRESHOLD = 0.8;
    public static final double GOOD_MATCH_THRESHOLD = 0.6;
    public static final double MODERATE_MATCH_THRESHOLD = 0.4;
    
    // Environment Variables
    public static final String ENV_ATLAS_CONNECTION_STRING = "ATLAS_CONNECTION_STRING";
    public static final String ENV_HUGGING_FACE_TOKEN = "HUGGING_FACE_ACCESS_TOKEN";
    
    // Error Messages
    public static final String ERROR_MISSING_CONNECTION_STRING = 
        "ATLAS_CONNECTION_STRING environment variable is not set or is empty";
    public static final String ERROR_MISSING_HF_TOKEN = 
        "HUGGING_FACE_ACCESS_TOKEN environment variable is not set or is empty";
    public static final String ERROR_EMBEDDING_GENERATION = 
        "Failed to generate embeddings";
    public static final String ERROR_JOB_MATCHING = 
        "Failed to perform job matching";
    
    private Constants() {
        // Utility class - prevent instantiation
    }
}

