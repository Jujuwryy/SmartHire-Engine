package com.george.exception;

/**
 * Error codes for programmatic error handling.
 * Each code represents a specific error condition that clients can handle programmatically.
 */
public enum ErrorCode {
    // Embedding errors (1000-1099)
    EMBEDDING_GENERATION_FAILED("EMB_001", "Embedding generation failed"),
    EMBEDDING_PROVIDER_ERROR("EMB_002", "Embedding provider error"),
    EMBEDDING_CACHE_ERROR("EMB_003", "Embedding cache error"),
    
    // Job matching errors (1100-1199)
    JOB_MATCHING_FAILED("JOB_001", "Job matching failed"),
    JOB_MATCHING_INVALID_INPUT("JOB_002", "Invalid input for job matching"),
    JOB_MATCHING_DATABASE_ERROR("JOB_003", "Database error during job matching"),
    JOB_MATCHING_EMBEDDING_ERROR("JOB_004", "Embedding error during job matching"),
    
    // Export errors (1200-1299)
    EXPORT_FAILED("EXP_001", "Export operation failed"),
    EXPORT_JSON_ERROR("EXP_002", "JSON export error"),
    EXPORT_CSV_ERROR("EXP_003", "CSV export error"),
    EXPORT_INVALID_FORMAT("EXP_004", "Invalid export format"),
    
    // Validation errors (2000-2099)
    VALIDATION_FAILED("VAL_001", "Request validation failed"),
    VALIDATION_CONSTRAINT_VIOLATION("VAL_002", "Constraint validation failed"),
    VALIDATION_INVALID_ARGUMENT("VAL_003", "Invalid argument provided"),
    VALIDATION_MISSING_FIELD("VAL_004", "Required field is missing"),
    
    // Rate limiting errors (3000-3099)
    RATE_LIMIT_EXCEEDED("RATE_001", "Rate limit exceeded"),
    RATE_LIMIT_GENERATE_EXCEEDED("RATE_002", "Rate limit exceeded for generate endpoint"),
    RATE_LIMIT_MATCH_EXCEEDED("RATE_003", "Rate limit exceeded for match endpoint"),
    
    // Security errors (4000-4099)
    SECURITY_ACCESS_DENIED("SEC_001", "Access denied"),
    SECURITY_UNAUTHORIZED("SEC_002", "Unauthorized access"),
    SECURITY_ACTUATOR_BLOCKED("SEC_003", "Actuator access blocked"),
    
    // Internal server errors (5000-5099)
    INTERNAL_SERVER_ERROR("INT_001", "Internal server error"),
    INTERNAL_UNEXPECTED_ERROR("INT_002", "An unexpected error occurred"),
    INTERNAL_DATABASE_ERROR("INT_003", "Database operation failed"),
    INTERNAL_SERVICE_UNAVAILABLE("INT_004", "Service temporarily unavailable");
    
    private final String code;
    private final String description;
    
    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}

