package com.george.util;

public class Constants {
    
    // API Endpoints - Read from application.yml via @Value in controllers
    public static final String API_V1_BASE = "/api/v1";
    public static final String VECTORS_ENDPOINT = API_V1_BASE + "/vectors";
    public static final String JOBS_ENDPOINT = API_V1_BASE + "/jobs";
    
    // Error Messages
    public static final String ERROR_EMBEDDING_GENERATION = "Failed to generate embeddings";
    public static final String ERROR_JOB_MATCHING = "Failed to perform job matching";
    
    private Constants() {
        // Utility class - prevent instantiation
    }
}

