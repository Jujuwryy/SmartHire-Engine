package com.george.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class TextPreprocessor {
    
    private static final Logger logger = LoggerFactory.getLogger(TextPreprocessor.class);
    
    // Pattern to remove excessive whitespace
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    
    // Pattern to remove special characters (keeping alphanumeric, spaces, and common punctuation)
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[^a-zA-Z0-9\\s.,!?;:()\\-]");
    
    // Preprocesses text before generating embeddings
    public static String preprocess(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        try {
            // Trim and normalize whitespace
            String processed = text.trim();
            processed = WHITESPACE_PATTERN.matcher(processed).replaceAll(" ");
            
            // Remove excessive special characters (optional - can be configured)
            // processed = SPECIAL_CHARS_PATTERN.matcher(processed).replaceAll("");
            
            return processed;
        } catch (Exception e) {
            logger.warn("Error preprocessing text, returning original", e);
            return text;
        }
    }
    
    /**
     * Validates text length for embedding generation
     * 
     * @param text Text to validate
     * @param maxLength Maximum allowed length
     * @return Truncated text if necessary
     */
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

