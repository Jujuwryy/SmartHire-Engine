package com.george.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.george.dto.JobMatchRequest;
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

    @Test
    void generateEndpoint_WithinRateLimit_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/vectors/generate"))
                .andExpect(status().isOk());
    }

    @Test
    void generateEndpoint_ExceedingRateLimit_ShouldReturn429() throws Exception {
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/v1/vectors/generate"));
        }
        
        mockMvc.perform(get("/api/v1/vectors/generate"))
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
                            .content(objectMapper.writeValueAsString(request)))
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
                            .content(objectMapper.writeValueAsString(request)));
        }
        
        mockMvc.perform(post("/api/v1/vectors/jobs/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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
                            .content(userProfile))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void matchSimpleEndpoint_ExceedingRateLimit_ShouldReturn429() throws Exception {
        String userProfile = "Experienced Java developer with Spring Boot knowledge";

        for (int i = 0; i < 100; i++) {
            mockMvc.perform(post("/api/v1/vectors/jobs/match/simple")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(userProfile));
        }
        
        mockMvc.perform(post("/api/v1/vectors/jobs/match/simple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userProfile))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Rate limit exceeded. Please try again later."));
    }
}

