package com.george.util;

import com.george.model.Post;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {
    
    public Post toPost(Document doc) {
        if (doc == null) {
            return null;
        }
        
        Post post = new Post();
        
        ObjectId id = doc.getObjectId("_id");
        if (id != null) {
            post.setId(id.toString());
        }
        
        post.setJobTitle(doc.getString("jobTitle"));
        post.setJobDescription(doc.getString("jobDescription"));
        post.setExperience(doc.getInteger("experience"));
        post.setRequiredTechs(doc.getList("requiredTechs", String.class));
        post.setCompany(doc.getString("company"));
        post.setLocation(doc.getString("location"));
        post.setEmploymentType(doc.getString("employmentType"));
        post.setSalaryMin(doc.getDouble("salaryMin"));
        post.setSalaryMax(doc.getDouble("salaryMax"));
        post.setCurrency(doc.getString("currency"));
        
        return post;
    }
}

