package com.george.util;

import com.george.config.AppProperties;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class MatchReasonGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(MatchReasonGenerator.class);
    
    @Autowired
    private AppProperties appProperties;
    
    // Common technology keywords and their variations
    private static final Set<String> TECH_KEYWORDS = Set.of(
        "java", "python", "javascript", "typescript", "react", "angular", "vue",
        "node", "spring", "django", "flask", "express", "mongodb", "postgresql",
        "mysql", "redis", "docker", "kubernetes", "aws", "azure", "gcp",
        "git", "ci/cd", "microservices", "rest", "graphql", "sql", "nosql"
    );
    
    public List<String> generateMatchReasons(Document doc, String userProfile) {
        List<String> reasons = new ArrayList<>();
        
        try {
            Double scoreObj = doc.getDouble("score");
            double score = scoreObj != null ? scoreObj : 0.0;
            
            double veryStrongThreshold = appProperties.getMatching().getThresholds().getVeryStrong();
            double goodThreshold = appProperties.getMatching().getThresholds().getGood();
            double moderateThreshold = appProperties.getMatching().getThresholds().getModerate();
            
            // Score-based reasons
            if (score >= veryStrongThreshold) {
                reasons.add("Very strong semantic match (confidence: " + String.format("%.2f", score) + ")");
            } else if (score >= goodThreshold) {
                reasons.add("Good semantic match (confidence: " + String.format("%.2f", score) + ")");
            } else if (score >= moderateThreshold) {
                reasons.add("Moderate semantic match (confidence: " + String.format("%.2f", score) + ")");
            }
            
            // Technology matching
            List<String> techs = doc.getList("requiredTechs", String.class);
            if (techs != null && !techs.isEmpty()) {
                String userProfileLower = userProfile.toLowerCase();
                List<String> matchingTechs = techs.stream()
                    .filter(tech -> {
                        String techLower = tech.toLowerCase();
                        return userProfileLower.contains(techLower) || 
                               TECH_KEYWORDS.stream().anyMatch(keyword -> 
                                   techLower.contains(keyword) && userProfileLower.contains(keyword));
                    })
                    .collect(Collectors.toList());
                
                if (!matchingTechs.isEmpty()) {
                    reasons.add("Matching technologies: " + String.join(", ", matchingTechs));
                }
            }
            
            // Experience level matching
            Integer requiredExp = doc.getInteger("experience");
            if (requiredExp != null) {
                if (extractExperienceFromProfile(userProfile) >= requiredExp) {
                    reasons.add("Experience level meets requirement (" + requiredExp + "+ years)");
                }
            }
            
            // Job title relevance
            String jobTitle = doc.getString("jobTitle");
            if (jobTitle != null && isTitleRelevant(jobTitle, userProfile)) {
                reasons.add("Job title aligns with profile");
            }
            
        } catch (Exception e) {
            logger.warn("Error generating match reasons", e);
            reasons.add("Match found");
        }
        
        return reasons.isEmpty() ? List.of("Potential match") : reasons;
    }
    
    private static int extractExperienceFromProfile(String profile) {
        // Simple regex to extract years of experience
        Pattern pattern = Pattern.compile("(\\d+)\\s*(?:years?|yrs?|yr)\\s*(?:of\\s*)?experience", 
            Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(profile);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    
    private static boolean isTitleRelevant(String jobTitle, String userProfile) {
        String titleLower = jobTitle.toLowerCase();
        String profileLower = userProfile.toLowerCase();
        
        // Extract key terms from job title
        String[] titleTerms = titleLower.split("[\\s-]+");
        for (String term : titleTerms) {
            if (term.length() > 3 && profileLower.contains(term)) {
                return true;
            }
        }
        return false;
    }
}

