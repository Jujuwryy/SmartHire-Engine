package com.george.model;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findByJobTitleContainingIgnoreCase(String jobTitle);
    List<Post> findByRequiredTechsContaining(String tech);
    List<Post> findByExperienceLessThanEqual(Integer experience);
}

