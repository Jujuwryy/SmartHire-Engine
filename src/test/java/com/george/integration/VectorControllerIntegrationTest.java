package com.george.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.george.dto.JobMatchRequest;
import com.george.dto.JobMatchResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class VectorControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void findMatchingJobs_WithValidRequest_ShouldReturnOk() throws Exception {
        JobMatchRequest request = new JobMatchRequest();
        request.setUserProfile("Experienced Java developer with Spring Boot knowledge");
        request.setLimit(10);
        request.setMinConfidence(0.0);

        MvcResult result = mockMvc.perform(post("/api/v1/vectors/jobs/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.matches").exists())
                .andExpect(jsonPath("$.totalMatches").exists())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        JobMatchResponse response = objectMapper.readValue(responseContent, JobMatchResponse.class);
        
        assertNotNull(response);
        assertNotNull(response.getMatches());
        assertTrue(response.getTotalMatches() >= 0);
    }

    @Test
    void findMatchingJobs_WithInvalidUserProfile_ShouldReturnBadRequest() throws Exception {
        JobMatchRequest request = new JobMatchRequest();
        request.setUserProfile("short");

        mockMvc.perform(post("/api/v1/vectors/jobs/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findMatchingJobs_WithInvalidLimit_ShouldReturnBadRequest() throws Exception {
        JobMatchRequest request = new JobMatchRequest();
        request.setUserProfile("Experienced Java developer with Spring Boot knowledge");
        request.setLimit(200);

        mockMvc.perform(post("/api/v1/vectors/jobs/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findMatchingJobs_WithInvalidMinConfidence_ShouldReturnBadRequest() throws Exception {
        JobMatchRequest request = new JobMatchRequest();
        request.setUserProfile("Experienced Java developer with Spring Boot knowledge");
        request.setMinConfidence(1.5);

        mockMvc.perform(post("/api/v1/vectors/jobs/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findMatchingJobs_WithNullUserProfile_ShouldReturnBadRequest() throws Exception {
        JobMatchRequest request = new JobMatchRequest();
        request.setUserProfile(null);

        mockMvc.perform(post("/api/v1/vectors/jobs/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findMatchingJobsSimple_WithValidProfile_ShouldReturnOk() throws Exception {
        String userProfile = "Experienced Java developer with Spring Boot knowledge";

        MvcResult result = mockMvc.perform(post("/api/v1/vectors/jobs/match/simple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userProfile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.matches").exists())
                .andExpect(jsonPath("$.totalMatches").exists())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        JobMatchResponse response = objectMapper.readValue(responseContent, JobMatchResponse.class);
        
        assertNotNull(response);
        assertNotNull(response.getMatches());
    }

    @Test
    void findMatchingJobsSimple_WithEmptyProfile_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/vectors/jobs/match/simple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findMatchingJobs_WithPreferredTechs_ShouldReturnOk() throws Exception {
        JobMatchRequest request = new JobMatchRequest();
        request.setUserProfile("Experienced Java developer with Spring Boot knowledge");
        request.setLimit(5);
        request.setPreferredTechs(java.util.Arrays.asList("Java", "Spring"));

        mockMvc.perform(post("/api/v1/vectors/jobs/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matches").exists());
    }

    @Test
    void findMatchingJobs_WithLocation_ShouldReturnOk() throws Exception {
        JobMatchRequest request = new JobMatchRequest();
        request.setUserProfile("Experienced Java developer with Spring Boot knowledge");
        request.setLocation("Remote");

        mockMvc.perform(post("/api/v1/vectors/jobs/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matches").exists());
    }

    @Test
    void findMatchingJobs_WithMaxExperience_ShouldReturnOk() throws Exception {
        JobMatchRequest request = new JobMatchRequest();
        request.setUserProfile("Experienced Java developer with Spring Boot knowledge");
        request.setMaxExperience(5);

        mockMvc.perform(post("/api/v1/vectors/jobs/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matches").exists());
    }
}

