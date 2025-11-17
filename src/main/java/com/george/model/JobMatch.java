package com.george.model;

import java.util.List;

public class JobMatch {
	
    private Post job;
    private double confidence;
    private List<String> matchReasons;
    
    public JobMatch() {}
    
    
    public JobMatch(Post job, double confidence, List<String> matchReasons) {
		super();
		this.job = job;
		this.confidence = confidence;
		this.matchReasons = matchReasons;
	}

	// Getters and setters
    public Post getJob() {
        return job;
    }

    public void setJob(Post job) {
        this.job = job;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public List<String> getMatchReasons() {
        return matchReasons;
    }

    public void setMatchReasons(List<String> matchReasons) {
        this.matchReasons = matchReasons;
    }
}
