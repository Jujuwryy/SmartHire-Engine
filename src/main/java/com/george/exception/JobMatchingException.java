package com.george.exception;

public class JobMatchingException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public JobMatchingException(String message) {
        super(message);
        this.errorCode = ErrorCode.JOB_MATCHING_FAILED;
    }
    
    public JobMatchingException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.JOB_MATCHING_FAILED;
    }
    
    public JobMatchingException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public JobMatchingException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

