package com.george.service;

import com.george.config.AppProperties;
import com.george.util.Constants;
import com.george.util.TextPreprocessor;
import org.springframework.stereotype.Component;

@Component
public class MatchingParameterNormalizer {
    
    private final AppProperties appProperties;
    
    public MatchingParameterNormalizer(AppProperties appProperties) {
        this.appProperties = appProperties;
    }
    
    public String normalizeUserProfile(String userProfile) {
        if (userProfile == null || userProfile.trim().isEmpty()) {
            throw new IllegalArgumentException("User profile cannot be null or empty");
        }
        
        String processed = TextPreprocessor.preprocess(userProfile);
        return TextPreprocessor.validateAndTruncate(processed, Constants.MAX_USER_PROFILE_LENGTH);
    }
    
    public int normalizeLimit(Integer limit) {
        int maxLimit = appProperties.getMatching().getMaxLimit();
        int defaultLimit = appProperties.getMatching().getDefaultLimit();
        
        if (limit != null && limit > 0 && limit <= maxLimit) {
            return limit;
        }
        return defaultLimit;
    }
    
    public double normalizeMinConfidence(Double minConfidence) {
        double defaultMinConf = appProperties.getMatching().getDefaultMinConfidence();
        
        if (minConfidence != null && minConfidence >= 0.0 && minConfidence <= 1.0) {
            return minConfidence;
        }
        return defaultMinConf;
    }
}

