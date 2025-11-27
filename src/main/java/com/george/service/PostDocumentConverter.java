package com.george.service;

import com.george.model.Post;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PostDocumentConverter {
    
    public Document toDocument(Post post) {
        List<String> techsList = post.getRequiredTechs() != null 
            ? new ArrayList<>(post.getRequiredTechs()) 
            : new ArrayList<>();
        
        Document doc = new Document()
            .append("jobTitle", post.getJobTitle())
            .append("jobDescription", post.getJobDescription())
            .append("experience", post.getExperience())
            .append("requiredTechs", techsList);
        
        if (post.getCompany() != null) {
            doc.append("company", post.getCompany());
        }
        if (post.getLocation() != null) {
            doc.append("location", post.getLocation());
        }
        if (post.getEmploymentType() != null) {
            doc.append("employmentType", post.getEmploymentType());
        }
        if (post.getSalaryMin() != null) {
            doc.append("salaryMin", post.getSalaryMin());
        }
        if (post.getSalaryMax() != null) {
            doc.append("salaryMax", post.getSalaryMax());
        }
        if (post.getCurrency() != null) {
            doc.append("currency", post.getCurrency());
        }
        
        return doc;
    }
}

