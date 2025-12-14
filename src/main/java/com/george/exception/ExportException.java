package com.george.exception;

public class ExportException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public ExportException(String message) {
        super(message);
        this.errorCode = ErrorCode.EXPORT_FAILED;
    }
    
    public ExportException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.EXPORT_FAILED;
    }
    
    public ExportException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ExportException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

