package com.george.service;

import com.george.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingParameterNormalizerTest {

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.Matching matchingProperties;

    private MatchingParameterNormalizer normalizer;

    @BeforeEach
    void setUp() {
        when(appProperties.getMatching()).thenReturn(matchingProperties);
        when(matchingProperties.getMaxLimit()).thenReturn(100);
        when(matchingProperties.getDefaultLimit()).thenReturn(10);
        when(matchingProperties.getDefaultMinConfidence()).thenReturn(0.0);
        normalizer = new MatchingParameterNormalizer(appProperties);
    }

    @Test
    void normalizeUserProfile_WithNull_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            normalizer.normalizeUserProfile(null);
        });
    }

    @Test
    void normalizeUserProfile_WithEmpty_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            normalizer.normalizeUserProfile("");
        });
    }

    @Test
    void normalizeLimit_WithNull_ReturnsDefault() {
        int result = normalizer.normalizeLimit(null);
        assertEquals(10, result);
    }

    @Test
    void normalizeLimit_WithValidLimit_ReturnsLimit() {
        int result = normalizer.normalizeLimit(20);
        assertEquals(20, result);
    }

    @Test
    void normalizeLimit_WithExceedingMax_ReturnsDefault() {
        int result = normalizer.normalizeLimit(200);
        assertEquals(10, result);
    }

    @Test
    void normalizeMinConfidence_WithNull_ReturnsDefault() {
        double result = normalizer.normalizeMinConfidence(null);
        assertEquals(0.0, result);
    }

    @Test
    void normalizeMinConfidence_WithValidConfidence_ReturnsConfidence() {
        double result = normalizer.normalizeMinConfidence(0.7);
        assertEquals(0.7, result);
    }

    @Test
    void normalizeMinConfidence_WithInvalidConfidence_ReturnsDefault() {
        double result = normalizer.normalizeMinConfidence(1.5);
        assertEquals(0.0, result);
    }
}

