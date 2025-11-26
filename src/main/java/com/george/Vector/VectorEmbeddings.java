package com.george.Vector;

import com.george.config.AppProperties;
import com.george.exception.EmbeddingException;
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
    
    private final AppProperties appProperties;

    public VectorEmbeddings(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    private HuggingFaceEmbeddingModel getEmbeddingModel() {
        if (embeddingModel == null) {
            synchronized (VectorEmbeddings.class) {
                if (embeddingModel == null) {
                    String accessToken = appProperties.getEmbeddings().getHuggingface().getAccessToken();
                    if (accessToken == null || accessToken.isEmpty()) {
                        throw new EmbeddingException("HUGGING_FACE_ACCESS_TOKEN environment variable is not set or is empty");
                    }
                    String modelId = appProperties.getEmbeddings().getHuggingface().getModelId();
                    int timeout = appProperties.getEmbeddings().getHuggingface().getTimeoutSeconds();
                    logger.info("Initializing HuggingFace embedding model: {}", modelId);
                    embeddingModel = HuggingFaceEmbeddingModel.builder()
                            .accessToken(accessToken)
                            .modelId(modelId)
                            .waitForModel(true)
                            .timeout(ofSeconds(timeout))
                            .build();
                    logger.info("HuggingFace embedding model initialized successfully");
                }
            }
        }
        return embeddingModel;
    }

    public List<BsonArray> getEmbeddings(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException("Text list cannot be null or empty");
        }
        
        try {
            List<TextSegment> textSegments = texts.stream()
                    .map(TextSegment::from)
                    .toList();
            
            Response<List<Embedding>> response = getEmbeddingModel().embedAll(textSegments);
            
            if (response == null || response.content() == null) {
                throw new EmbeddingException("Received null response from embedding model");
            }
            
            return response.content().stream()
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
        } catch (EmbeddingException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new EmbeddingException("Failed to generate embeddings", e);
        }
    }

    public BsonArray getEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }
        
        try {
            Response<Embedding> response = getEmbeddingModel().embed(text);
            
            if (response == null || response.content() == null) {
                throw new EmbeddingException("Received null response from embedding model");
            }
            
            return new BsonArray(
                    response.content().vectorAsList().stream()
                            .map(BsonDouble::new)
                            .toList());
        } catch (EmbeddingException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new EmbeddingException("Failed to generate embedding", e);
        }
    }
}

