package com.george.util;

public final class Constants {
    
    public static final int MAX_USER_PROFILE_LENGTH = 2000;
    public static final int MIN_USER_PROFILE_LENGTH = 10;
    public static final int MAX_ARGUMENT_DISPLAY_LENGTH = 100;
    public static final int MAX_RESULT_DISPLAY_LENGTH = 200;
    public static final long SLOW_OPERATION_THRESHOLD_MS = 1000;
    public static final int CACHE_MAX_SIZE = 1000;
    
    private Constants() {
        throw new UnsupportedOperationException("Utility class");
    }
}

