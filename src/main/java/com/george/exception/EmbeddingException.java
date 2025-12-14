package com.george.exception;

public class EmbeddingException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public EmbeddingException(String message) {
        super(message);
        this.errorCode = ErrorCode.EMBEDDING_GENERATION_FAILED;
    }
    
    public EmbeddingException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.EMBEDDING_GENERATION_FAILED;
    }
    
    public EmbeddingException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public EmbeddingException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

