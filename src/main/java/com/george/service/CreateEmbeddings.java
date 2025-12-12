package com.george.service;

import com.george.config.AppProperties;
import com.george.exception.EmbeddingException;
import com.george.model.Post;
import com.george.model.PostRepository;
import com.george.service.api.EmbeddingProvider;
import com.george.service.PostDocumentConverter;
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

    private final EmbeddingProvider embeddingProvider;
    private final PostRepository postRepository;
    private final AppProperties appProperties;
    private final MongoClient mongoClient;
    private final PostDocumentConverter documentConverter;

    public CreateEmbeddings(EmbeddingProvider embeddingProvider,
                            PostRepository postRepository,
                            AppProperties appProperties,
                            MongoClient mongoClient,
                            PostDocumentConverter documentConverter) {
        this.embeddingProvider = embeddingProvider;
        this.postRepository = postRepository;
        this.appProperties = appProperties;
        this.mongoClient = mongoClient;
        this.documentConverter = documentConverter;
    }

    public void createEmbeddings() {
        if (postRepository == null) {
            throw new IllegalStateException("PostRepository is not available");
        }
        if (embeddingProvider == null) {
            throw new IllegalStateException("EmbeddingProvider is not available");
        }
        if (mongoClient == null) {
            throw new IllegalStateException("MongoDB client is not available");
        }
        if (appProperties == null || appProperties.getMongodb() == null) {
            throw new IllegalStateException("MongoDB configuration is not available");
        }
        if (documentConverter == null) {
            throw new IllegalStateException("PostDocumentConverter is not available");
        }

        List<Post> existingPosts = postRepository.findAll();
        
        if (existingPosts == null || existingPosts.isEmpty()) {
            throw new EmbeddingException("No job posts found in repository");
        }
        
        logger.info("Processing {} job posts for embedding generation", existingPosts.size());
        
        try {
            String databaseName = appProperties.getMongodb().getDatabaseName();
            String collectionName = appProperties.getMongodb().getCollectionName();
            
            if (databaseName == null || databaseName.trim().isEmpty()) {
                throw new IllegalStateException("Database name is not configured");
            }
            if (collectionName == null || collectionName.trim().isEmpty()) {
                throw new IllegalStateException("Collection name is not configured");
            }
            
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            if (database == null) {
                throw new EmbeddingException("Failed to access MongoDB database: " + databaseName);
            }
            
            MongoCollection<Document> collection = database.getCollection(collectionName);
            if (collection == null) {
                throw new EmbeddingException("Failed to access MongoDB collection: " + collectionName);
            }

            List<Document> documents = new ArrayList<>();
            List<String> descriptions = new ArrayList<>();
            
            for (Post post : existingPosts) {
                if (post == null) {
                    logger.warn("Skipping null post in embedding generation");
                    continue;
                }
                if (post.getJobDescription() == null || post.getJobDescription().trim().isEmpty()) {
                    logger.warn("Skipping post with empty description: {}", 
                        post.getJobTitle() != null ? post.getJobTitle() : "Unknown");
                    continue;
                }
                
                Document doc = documentConverter.toDocument(post);
                if (doc == null) {
                    logger.warn("Failed to convert post to document, skipping");
                    continue;
                }
                
                documents.add(doc);
                descriptions.add(post.getJobDescription());
            }

            if (descriptions.isEmpty()) {
                throw new EmbeddingException("No valid job descriptions found to generate embeddings");
            }
            
            List<BsonArray> embeddings = embeddingProvider.getEmbeddings(descriptions);
            if (embeddings == null) {
                throw new EmbeddingException("Failed to generate embeddings - received null result");
            }

            if (embeddings.size() != documents.size()) {
                throw new EmbeddingException(
                    String.format("Mismatch between embeddings count (%d) and documents count (%d)", 
                        embeddings.size(), documents.size()));
            }

            for (int i = 0; i < documents.size(); i++) {
                if (embeddings.get(i) == null) {
                    throw new EmbeddingException("Received null embedding at index " + i);
                }
                documents.get(i).append("embedding", embeddings.get(i));
            }

            InsertManyResult result = collection.insertMany(documents);
            if (result == null) {
                throw new EmbeddingException("Failed to insert documents - received null result");
            }
            logger.info("Successfully inserted {} documents with embeddings", result.getInsertedIds().size());
        } catch (MongoException me) {
            throw new EmbeddingException("Failed to perform MongoDB operation", me);
        } catch (EmbeddingException e) {
            throw e;
        } catch (IllegalStateException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new EmbeddingException("Failed to generate embeddings", e);
        }
    }
}
