package com.george.dto;

import com.george.model.JobMatch;
import java.util.List;

public class JobMatchResponse {
    private List<JobMatch> matches;
    private Integer totalMatches;
    private Long queryTimeMs;
    private String queryId;
    
    public JobMatchResponse() {}
    
    public JobMatchResponse(List<JobMatch> matches) {
        this.matches = matches;
        this.totalMatches = matches != null ? matches.size() : 0;
    }
    
    // Getters and setters
    public List<JobMatch> getMatches() {
        return matches;
    }
    
    public void setMatches(List<JobMatch> matches) {
        this.matches = matches;
        this.totalMatches = matches != null ? matches.size() : 0;
    }
    
    public Integer getTotalMatches() {
        return totalMatches;
    }
    
    public void setTotalMatches(Integer totalMatches) {
        this.totalMatches = totalMatches;
    }
    
    public Long getQueryTimeMs() {
        return queryTimeMs;
    }
    
    public void setQueryTimeMs(Long queryTimeMs) {
        this.queryTimeMs = queryTimeMs;
    }
    
    public String getQueryId() {
        return queryId;
    }
    
    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }
}

