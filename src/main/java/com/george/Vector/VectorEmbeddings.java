package com.george.Vector;

import com.george.exception.EmbeddingException;
import com.george.util.Constants;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.bson.BsonArray;
import org.bson.BsonDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import static java.time.Duration.ofSeconds;

@Service
public class VectorEmbeddings {

    private static final Logger logger = LoggerFactory.getLogger(VectorEmbeddings.class);
    private static HuggingFaceEmbeddingModel embeddingModel;

    // Returns an instance of HuggingFaceEmbeddingModel with appropriate configurations
    private static HuggingFaceEmbeddingModel getEmbeddingModel() {
        if (embeddingModel == null) {
            synchronized (VectorEmbeddings.class) {
                if (embeddingModel == null) {
                    String accessToken = System.getenv(Constants.ENV_HUGGING_FACE_TOKEN);
                    if (accessToken == null || accessToken.isEmpty()) {
                        throw new EmbeddingException(Constants.ERROR_MISSING_HF_TOKEN);
                    }
                    logger.info("Initializing HuggingFace embedding model: {}", Constants.EMBEDDING_MODEL_ID);
                    embeddingModel = HuggingFaceEmbeddingModel.builder()
                            .accessToken(accessToken)
                            .modelId(Constants.EMBEDDING_MODEL_ID)
                            .waitForModel(true)
                            .timeout(ofSeconds(Constants.EMBEDDING_TIMEOUT_SECONDS))
                            .build();
                    logger.info("HuggingFace embedding model initialized successfully");
                }
            }
        }
        return embeddingModel;
    }

    /**
     * Takes an array of strings and returns a BSON array of embeddings to
     * store in the database.
     *
     * @param texts List of strings to generate embeddings for.
     * @return List of BSON arrays representing embeddings.
     * @throws EmbeddingException if embedding generation fails
     */
    public List<BsonArray> getEmbeddings(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException("Text list cannot be null or empty");
        }
        
        logger.debug("Generating embeddings for {} texts", texts.size());
        long startTime = System.currentTimeMillis();
        
        try {
            List<TextSegment> textSegments = texts.stream()
                    .map(TextSegment::from)
                    .toList();
            
            Response<List<Embedding>> response = getEmbeddingModel().embedAll(textSegments);
            
            if (response == null || response.content() == null) {
                throw new EmbeddingException("Received null response from embedding model");
            }
            
            List<BsonArray> embeddings = response.content().stream()
                    .map(e -> {
                        if (e == null || e.vectorAsList() == null) {
                            throw new EmbeddingException("Received null embedding vector");
                        }
                        return new BsonArray(
                                e.vectorAsList().stream()
                                        .map(BsonDouble::new)
                                        .toList());
                    })
                    .toList();
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Generated {} embeddings in {}ms", embeddings.size(), duration);
            
            return embeddings;
        } catch (EmbeddingException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error generating embeddings", e);
            throw new EmbeddingException("Failed to generate embeddings", e);
        }
    }

    /**
     * Takes a single string and returns a BSON array embedding to
     * use in a vector query.
     *
     * @param text The string to generate embedding for.
     * @return BSON array representing the embedding.
     * @throws EmbeddingException if embedding generation fails
     */
    public BsonArray getEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }
        
        logger.debug("Generating embedding for text (length: {})", text.length());
        long startTime = System.currentTimeMillis();
        
        try {
            Response<Embedding> response = getEmbeddingModel().embed(text);
            
            if (response == null || response.content() == null) {
                throw new EmbeddingException("Received null response from embedding model");
            }
            
            BsonArray embedding = new BsonArray(
                    response.content().vectorAsList().stream()
                            .map(BsonDouble::new)
                            .toList());
            
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Generated embedding in {}ms (dimension: {})", duration, embedding.size());
            
            return embedding;
        } catch (EmbeddingException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error generating embedding", e);
            throw new EmbeddingException("Failed to generate embedding", e);
        }
    }
}

