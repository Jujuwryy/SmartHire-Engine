package com.george;

import com.george.Vector.VectorEmbeddings;
import com.george.config.AppProperties;
import com.george.exception.EmbeddingException;
import com.george.model.Post;
import com.george.model.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertManyResult;
import org.bson.BsonArray;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CreateEmbeddings {

    private static final Logger logger = LoggerFactory.getLogger(CreateEmbeddings.class);

    @Autowired
    private VectorEmbeddings embeddingProvider;
    
    @Autowired
    private PostRepository repo;

    public void createEmbeddings() {
        String uri = System.getenv(Constants.ENV_ATLAS_CONNECTION_STRING);
        if (uri == null || uri.isEmpty()) {
            throw new EmbeddingException(Constants.ERROR_MISSING_CONNECTION_STRING);
        }

        logger.info("Starting embedding generation process");
        
        // Fetch existing posts from repository
        List<Post> existingPosts = repo.findAll();
        
        if (existingPosts == null || existingPosts.isEmpty()) {
            logger.warn("No posts found in repository to generate embeddings for");
            throw new EmbeddingException("No job posts found in repository");
        }
        
        logger.info("Found {} posts to process", existingPosts.size());
        
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase(Constants.DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(Constants.COLLECTION_NAME);

            // Convert Posts to Documents and extract descriptions
            List<Document> documents = new ArrayList<>();
            List<String> descriptions = new ArrayList<>();
            
            for (Post post : existingPosts) {
                if (post.getJobDescription() == null || post.getJobDescription().trim().isEmpty()) {
                    logger.warn("Skipping post with empty description: {}", post.getJobTitle());
                    continue;
                }
                
                // Convert requiredTechs to ArrayList to ensure proper MongoDB serialization
                ArrayList<String> techsList = post.getRequiredTechs() != null 
                    ? new ArrayList<>(post.getRequiredTechs()) 
                    : new ArrayList<>();
                
                Document doc = new Document()
                    .append("jobTitle", post.getJobTitle())
                    .append("jobDescription", post.getJobDescription())
                    .append("experience", post.getExperience())
                    .append("requiredTechs", techsList);
                
                // Add optional fields if present
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

            logger.info("Generating embeddings for {} job descriptions", descriptions.size());
            
            // Generate embeddings
            List<BsonArray> embeddings = embeddingProvider.getEmbeddings(descriptions);

            if (embeddings.size() != documents.size()) {
                throw new EmbeddingException(
                    String.format("Mismatch between embeddings count (%d) and documents count (%d)", 
                        embeddings.size(), documents.size()));
            }

            // Add embeddings to documents
            for (int i = 0; i < documents.size(); i++) {
                documents.get(i).append("embedding", embeddings.get(i));
            }

            logger.info("Inserting {} documents with embeddings into MongoDB", documents.size());
            
            // Insert documents with embeddings
            try {
                InsertManyResult result = collection.insertMany(documents);
                List<String> insertedIds = new ArrayList<>();
                result.getInsertedIds().values()
                    .forEach(doc -> insertedIds.add(doc.toString()));
                logger.info("Successfully inserted {} documents with embeddings to collection: {}", 
                    insertedIds.size(), collection.getNamespace());
                logger.debug("Inserted document IDs: {}", insertedIds);
            } catch (MongoException me) {
                logger.error("Failed to insert documents into MongoDB", me);
                throw new EmbeddingException("Failed to insert documents into MongoDB", me);
            }
        } catch (MongoException me) {
            logger.error("Failed to connect to MongoDB", me);
            throw new EmbeddingException("Failed to connect to MongoDB", me);
        } catch (EmbeddingException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during embedding generation", e);
            throw new EmbeddingException("Failed to generate embeddings", e);
        }
        
        logger.info("Embedding generation process completed successfully");
    }
}
