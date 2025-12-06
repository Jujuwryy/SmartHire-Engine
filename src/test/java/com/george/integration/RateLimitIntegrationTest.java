package com.george.integration;

import com.george.config.RateLimitConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.george.dto.JobMatchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class RateLimitIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RateLimitConfig.RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        rateLimitFilter.clearCache();
    }

    @Test
    void generateEndpoint_WithinRateLimit_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/vectors/generate")
                        .header("X-Forwarded-For", "127.0.0.10"))
                .andExpect(status().isOk());
    }

    @Test
    void generateEndpoint_ExceedingRateLimit_ShouldReturn429() throws Exception {
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/v1/vectors/generate")
                            .header("X-Forwarded-For", "127.0.0.11"));
        }
        
        mockMvc.perform(get("/api/v1/vectors/generate")
                        .header("X-Forwarded-For", "127.0.0.11"))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Rate limit exceeded. Please try again later."));
    }

    @Test
    void matchEndpoint_WithinRateLimit_ShouldReturnOk() throws Exception {
        JobMatchRequest request = new JobMatchRequest();
        request.setUserProfile("Experienced Java developer with Spring Boot knowledge");

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/vectors/jobs/match")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .header("X-Forwarded-For", "127.0.0.50"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void matchEndpoint_ExceedingRateLimit_ShouldReturn429() throws Exception {
        JobMatchRequest request = new JobMatchRequest();
        request.setUserProfile("Experienced Java developer with Spring Boot knowledge");

        for (int i = 0; i < 100; i++) {
            mockMvc.perform(post("/api/v1/vectors/jobs/match")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .header("X-Forwarded-For", "127.0.0.100"));
        }
        
        mockMvc.perform(post("/api/v1/vectors/jobs/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Forwarded-For", "127.0.0.100"))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Rate limit exceeded. Please try again later."));
    }

    @Test
    void matchSimpleEndpoint_WithinRateLimit_ShouldReturnOk() throws Exception {
        String userProfile = "Experienced Java developer with Spring Boot knowledge";

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/vectors/jobs/match/simple")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(userProfile)
                            .header("X-Forwarded-For", "127.0.0.200"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void matchSimpleEndpoint_ExceedingRateLimit_ShouldReturn429() throws Exception {
        String userProfile = "Experienced Java developer with Spring Boot knowledge";

        for (int i = 0; i < 100; i++) {
            mockMvc.perform(post("/api/v1/vectors/jobs/match/simple")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(userProfile)
                            .header("X-Forwarded-For", "127.0.0.30"));
        }
        
        mockMvc.perform(post("/api/v1/vectors/jobs/match/simple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userProfile)
                        .header("X-Forwarded-For", "127.0.0.30"))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Rate limit exceeded. Please try again later."));
    }
}