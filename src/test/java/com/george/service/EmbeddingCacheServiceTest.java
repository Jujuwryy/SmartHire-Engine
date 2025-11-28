package com.george.service;

import com.george.service.api.EmbeddingProvider;
import org.bson.BsonArray;
import org.bson.BsonDouble;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmbeddingCacheServiceTest {

    @Mock
    private EmbeddingProvider embeddingProvider;

    @InjectMocks
    private EmbeddingCacheService embeddingCacheService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getCachedEmbedding_WithNullText_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            embeddingCacheService.getCachedEmbedding(null);
        });
    }

    @Test
    void getCachedEmbedding_WithEmptyText_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            embeddingCacheService.getCachedEmbedding("");
        });
    }

    @Test
    void getCachedEmbedding_WithWhitespaceOnly_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            embeddingCacheService.getCachedEmbedding("   ");
        });
    }

    @Test
    void getCachedEmbedding_WithValidText_ReturnsEmbedding() {
        String text = "Java developer";
        BsonArray expectedEmbedding = createMockEmbedding();
        
        when(embeddingProvider.getEmbedding(text)).thenReturn(expectedEmbedding);

        BsonArray result = embeddingCacheService.getCachedEmbedding(text);

        assertNotNull(result);
        assertEquals(expectedEmbedding.size(), result.size());
        verify(embeddingProvider).getEmbedding(text);
    }

    @Test
    void getCachedEmbedding_WithNullEmbedding_ThrowsException() {
        String text = "Java developer";
        
        when(embeddingProvider.getEmbedding(text)).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> {
            embeddingCacheService.getCachedEmbedding(text);
        });
    }

    private BsonArray createMockEmbedding() {
        BsonArray embedding = new BsonArray();
        for (int i = 0; i < 10; i++) {
            embedding.add(new BsonDouble(0.1 * i));
        }
        return embedding;
    }
}

