package com.george;

import com.george.Vector.VectorEmbeddings;
import com.george.config.AppProperties;
import com.george.exception.EmbeddingException;
import com.george.model.Post;
import com.george.model.PostRepository;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertManyResult;
import org.bson.BsonArray;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CreateEmbeddings {

    private static final Logger logger = LoggerFactory.getLogger(CreateEmbeddings.class);

    private final VectorEmbeddings embeddingProvider;
    private final PostRepository postRepository;
    private final AppProperties appProperties;
    private final MongoClient mongoClient;

    public CreateEmbeddings(VectorEmbeddings embeddingProvider,
                            PostRepository postRepository,
                            AppProperties appProperties,
                            MongoClient mongoClient) {
        this.embeddingProvider = embeddingProvider;
        this.postRepository = postRepository;
        this.appProperties = appProperties;
        this.mongoClient = mongoClient;
    }

    public void createEmbeddings() {
        List<Post> existingPosts = postRepository.findAll();
        
        if (existingPosts.isEmpty()) {
            throw new EmbeddingException("No job posts found in repository");
        }
        
        logger.info("Processing {} job posts for embedding generation", existingPosts.size());
        
        try {
            String databaseName = appProperties.getMongodb().getDatabaseName();
            String collectionName = appProperties.getMongodb().getCollectionName();
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(collectionName);

            List<Document> documents = new ArrayList<>();
            List<String> descriptions = new ArrayList<>();
            
            for (Post post : existingPosts) {
                if (post.getJobDescription() == null || post.getJobDescription().trim().isEmpty()) {
                    logger.warn("Skipping post with empty description: {}", post.getJobTitle());
                    continue;
                }
                
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
                
                documents.add(doc);
                descriptions.add(post.getJobDescription());
            }

            if (descriptions.isEmpty()) {
                throw new EmbeddingException("No valid job descriptions found to generate embeddings");
            }
            
            List<BsonArray> embeddings = embeddingProvider.getEmbeddings(descriptions);

            if (embeddings.size() != documents.size()) {
                throw new EmbeddingException(
                    String.format("Mismatch between embeddings count (%d) and documents count (%d)", 
                        embeddings.size(), documents.size()));
            }

            for (int i = 0; i < documents.size(); i++) {
                documents.get(i).append("embedding", embeddings.get(i));
            }

            InsertManyResult result = collection.insertMany(documents);
            logger.info("Successfully inserted {} documents with embeddings", result.getInsertedIds().size());
        } catch (MongoException me) {
            throw new EmbeddingException("Failed to perform MongoDB operation", me);
        } catch (EmbeddingException e) {
            throw e;
        } catch (Exception e) {
            throw new EmbeddingException("Failed to generate embeddings", e);
        }
    }
}
