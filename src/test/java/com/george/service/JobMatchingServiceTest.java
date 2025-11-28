package com.george.service;

import com.george.config.AppProperties;
import com.george.dto.JobMatchRequest;
import com.george.exception.JobMatchingException;
import com.george.model.JobMatch;
import com.george.util.DocumentMapper;
import com.george.util.MatchReasonGenerator;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonArray;
import org.bson.BsonDouble;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobMatchingServiceTest {

    @Mock
    private MongoClient mongoClient;

    @Mock
    private EmbeddingCacheService embeddingCacheService;

    @Mock
    private AppProperties appProperties;

    @Mock
    private MatchReasonGenerator matchReasonGenerator;

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private MatchingParameterNormalizer parameterNormalizer;

    @Mock
    private VectorSearchQueryBuilder queryBuilder;

    @Mock
    private MongoDatabase mongoDatabase;

    @Mock
    private MongoCollection<Document> mongoCollection;

    @Mock
    private AggregateIterable<Document> aggregateIterable;

    @Mock
    private AppProperties.Mongodb mongodbProperties;

    @Mock
    private AppProperties.Matching matchingProperties;

    @InjectMocks
    private JobMatchingService jobMatchingService;

    @BeforeEach
    void setUp() {
        when(appProperties.getMongodb()).thenReturn(mongodbProperties);
        when(appProperties.getMatching()).thenReturn(matchingProperties);
        when(mongodbProperties.getDatabaseName()).thenReturn("test_db");
        when(mongodbProperties.getCollectionName()).thenReturn("test_collection");
        when(matchingProperties.getDefaultLimit()).thenReturn(10);
        when(matchingProperties.getDefaultMinConfidence()).thenReturn(0.0);
        when(mongoClient.getDatabase(anyString())).thenReturn(mongoDatabase);
        when(mongoDatabase.getCollection(anyString())).thenReturn(mongoCollection);
    }

    @Test
    void findMatchingJobs_WithNullUserProfile_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            jobMatchingService.findMatchingJobs((String) null);
        });
    }

    @Test
    void findMatchingJobs_WithEmptyUserProfile_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            jobMatchingService.findMatchingJobs("");
        });
    }

    @Test
    void findMatchingJobs_WithNullRequest_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            jobMatchingService.findMatchingJobs((JobMatchRequest) null);
        });
    }

    @Test
    void findMatchingJobsSimple_WithNullUserProfile_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            jobMatchingService.findMatchingJobsSimple(null);
        });
    }

    @Test
    void findMatchingJobsSimple_WithUserProfileExceedingLimit_ThrowsException() {
        String longProfile = "a".repeat(2001);
        assertThrows(IllegalArgumentException.class, () -> {
            jobMatchingService.findMatchingJobsSimple(longProfile);
        });
    }

    @Test
    void findMatchingJobs_WithValidRequest_ReturnsMatches() {
        String userProfile = "Experienced Java developer with 5 years of experience";
        BsonArray embedding = createMockEmbedding();
        
        when(parameterNormalizer.normalizeUserProfile(anyString())).thenReturn(userProfile);
        when(parameterNormalizer.normalizeLimit(anyInt())).thenReturn(10);
        when(parameterNormalizer.normalizeMinConfidence(anyDouble())).thenReturn(0.0);
        when(embeddingCacheService.getCachedEmbedding(anyString())).thenReturn(embedding);
        when(queryBuilder.buildSearchPipeline(any(), anyInt(), anyDouble())).thenReturn(createMockPipeline());
        
        List<Document> mockDocuments = createMockDocuments();
        when(mongoCollection.aggregate(anyList())).thenReturn(aggregateIterable);
        doAnswer(invocation -> {
            java.util.function.Consumer<Document> consumer = invocation.getArgument(0);
            mockDocuments.forEach(consumer);
            return null;
        }).when(aggregateIterable).forEach(any());
        when(documentMapper.toPost(any(Document.class))).thenReturn(createMockPost());
        when(matchReasonGenerator.generateMatchReasons(any(Document.class), anyString())).thenReturn(List.of("Strong match"));

        List<JobMatch> result = jobMatchingService.findMatchingJobs(userProfile, 10, 0.0);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(embeddingCacheService).getCachedEmbedding(anyString());
        verify(mongoCollection).aggregate(anyList());
    }

    @Test
    void findMatchingJobs_WithNullEmbedding_ThrowsException() {
        String userProfile = "Java developer";
        
        when(parameterNormalizer.normalizeUserProfile(anyString())).thenReturn(userProfile);
        when(parameterNormalizer.normalizeLimit(anyInt())).thenReturn(10);
        when(parameterNormalizer.normalizeMinConfidence(anyDouble())).thenReturn(0.0);
        when(embeddingCacheService.getCachedEmbedding(anyString())).thenReturn(null);

        assertThrows(JobMatchingException.class, () -> {
            jobMatchingService.findMatchingJobs(userProfile, 10, 0.0);
        });
    }

    private BsonArray createMockEmbedding() {
        BsonArray embedding = new BsonArray();
        for (int i = 0; i < 10; i++) {
            embedding.add(new BsonDouble(0.1 * i));
        }
        return embedding;
    }

    private List<Document> createMockPipeline() {
        return new ArrayList<>();
    }

    private List<Document> createMockDocuments() {
        Document doc = new Document();
        doc.append("score", 0.85);
        doc.append("jobTitle", "Senior Java Developer");
        doc.append("jobDescription", "Looking for experienced Java developer");
        return List.of(doc);
    }

    private com.george.model.Post createMockPost() {
        com.george.model.Post post = new com.george.model.Post();
        post.setJobTitle("Senior Java Developer");
        post.setJobDescription("Looking for experienced Java developer");
        return post;
    }
}

