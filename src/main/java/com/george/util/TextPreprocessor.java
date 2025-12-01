package com.george.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class TextPreprocessor {
    
    private static final Logger logger = LoggerFactory.getLogger(TextPreprocessor.class);
    
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    
    public static boolean isNullOrEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }
    
    public static String preprocess(String text) {
        if (isNullOrEmpty(text)) {
            return "";
        }
        
        try {
            String processed = text.trim();
            processed = WHITESPACE_PATTERN.matcher(processed).replaceAll(" ");
            return processed;
        } catch (Exception e) {
            logger.warn("Error preprocessing text, returning original", e);
            return text;
        }
    }
    
    public static String validateAndTruncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        
        if (text.length() > maxLength) {
            logger.debug("Truncating text from {} to {} characters", text.length(), maxLength);
            return text.substring(0, maxLength);
        }
        
        return text;
    }
}

