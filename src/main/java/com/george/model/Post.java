package com.george.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "JobPost")
public class Post {
    
    @Id
    private String id;
    private String jobTitle;
    private String jobDescription;
    private Integer experience;
    private List<String> requiredTechs;
    private String company;
    private String location;
    private String employmentType;
    private Double salaryMin;
    private Double salaryMax;
    private String currency;
    
    public Post() {}
    
    public Post(String jobTitle, String jobDescription, Integer experience, List<String> requiredTechs) {
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
        this.experience = experience;
        this.requiredTechs = requiredTechs;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getJobTitle() {
        return jobTitle;
    }
    
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    
    public String getJobDescription() {
        return jobDescription;
    }
    
    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }
    
    public Integer getExperience() {
        return experience;
    }
    
    public void setExperience(Integer experience) {
        this.experience = experience;
    }
    
    public List<String> getRequiredTechs() {
        return requiredTechs;
    }
    
    public void setRequiredTechs(List<String> requiredTechs) {
        this.requiredTechs = requiredTechs;
    }
    
    public String getCompany() {
        return company;
    }
    
    public void setCompany(String company) {
        this.company = company;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getEmploymentType() {
        return employmentType;
    }
    
    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }
    
    public Double getSalaryMin() {
        return salaryMin;
    }
    
    public void setSalaryMin(Double salaryMin) {
        this.salaryMin = salaryMin;
    }
    
    public Double getSalaryMax() {
        return salaryMax;
    }
    
    public void setSalaryMax(Double salaryMax) {
        this.salaryMax = salaryMax;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
}

