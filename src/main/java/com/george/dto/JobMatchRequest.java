package com.george.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class JobMatchRequest {
    
    @NotBlank(message = "User profile cannot be blank")
    @Size(min = 10, max = 2000, message = "User profile must be between 10 and 2000 characters")
    private String userProfile;
    
    private Integer limit = 10;
    private Double minConfidence = 0.0;
    private List<String> preferredTechs;
    private String location;
    private Integer maxExperience;
    
    public JobMatchRequest() {}
    
    public JobMatchRequest(String userProfile) {
        this.userProfile = userProfile;
    }
    
    // Getters and setters
    public String getUserProfile() {
        return userProfile;
    }
    
    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }
    
    public Integer getLimit() {
        return limit;
    }
    
    public void setLimit(Integer limit) {
        this.limit = limit != null && limit > 0 && limit <= 100 ? limit : 10;
    }
    
    public Double getMinConfidence() {
        return minConfidence;
    }
    
    public void setMinConfidence(Double minConfidence) {
        this.minConfidence = minConfidence != null && minConfidence >= 0.0 && minConfidence <= 1.0 
            ? minConfidence : 0.0;
    }
    
    public List<String> getPreferredTechs() {
        return preferredTechs;
    }
    
    public void setPreferredTechs(List<String> preferredTechs) {
        this.preferredTechs = preferredTechs;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public Integer getMaxExperience() {
        return maxExperience;
    }
    
    public void setMaxExperience(Integer maxExperience) {
        this.maxExperience = maxExperience;
    }
}

