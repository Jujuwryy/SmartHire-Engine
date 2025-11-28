package com.george.controller;

import com.george.dto.JobMatchRequest;
import com.george.dto.JobMatchResponse;
import com.george.model.JobMatch;
import com.george.service.CreateEmbeddings;
import com.george.service.JobMatchingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VectorController.class)
class VectorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateEmbeddings createEmbeddingsService;

    @MockBean
    private JobMatchingService jobMatchingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generateEmbeddings_ShouldReturnSuccess() throws Exception {
        doNothing().when(createEmbeddingsService).createEmbeddings();

        mockMvc.perform(get("/api/v1/vectors/generate"))
                .andExpect(status().isOk())
                .andExpect(content().string("Embeddings generated and saved successfully!"));
    }

    @Test
    void findMatchingJobs_WithValidRequest_ShouldReturnMatches() throws Exception {
        JobMatchRequest request = new JobMatchRequest();
        request.setUserProfile("Experienced Java developer");
        request.setLimit(10);
        request.setMinConfidence(0.6);

        List<JobMatch> matches = new ArrayList<>();
        when(jobMatchingService.findMatchingJobs(any(JobMatchRequest.class))).thenReturn(matches);

        mockMvc.perform(post("/api/v1/vectors/jobs/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matches").exists())
                .andExpect(jsonPath("$.totalMatches").exists());
    }

    @Test
    void findMatchingJobs_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        JobMatchRequest request = new JobMatchRequest();
        request.setUserProfile("ab");

        mockMvc.perform(post("/api/v1/vectors/jobs/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findMatchingJobsSimple_WithValidProfile_ShouldReturnMatches() throws Exception {
        String userProfile = "Experienced Java developer";
        List<JobMatch> matches = new ArrayList<>();
        
        when(jobMatchingService.findMatchingJobsSimple(userProfile)).thenReturn(matches);

        mockMvc.perform(post("/api/v1/vectors/jobs/match/simple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userProfile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matches").exists());
    }
}

