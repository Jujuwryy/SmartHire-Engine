package com.george.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.george.dto.JobMatchRequest;
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
class SecurityIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void apiEndpoints_ShouldHaveSecurityHeaders() throws Exception {
        JobMatchRequest request = new JobMatchRequest();
        request.setUserProfile("Experienced Java developer with Spring Boot knowledge");

        MvcResult result = mockMvc.perform(post("/api/v1/vectors/jobs/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        assertNotNull(result.getResponse().getHeader("X-Content-Type-Options"));
        assertEquals("nosniff", result.getResponse().getHeader("X-Content-Type-Options"));
        
        assertNotNull(result.getResponse().getHeader("X-Frame-Options"));
        assertEquals("DENY", result.getResponse().getHeader("X-Frame-Options"));
        
        assertNotNull(result.getResponse().getHeader("X-XSS-Protection"));
        assertEquals("1; mode=block", result.getResponse().getHeader("X-XSS-Protection"));
        
        assertNotNull(result.getResponse().getHeader("Strict-Transport-Security"));
        assertTrue(result.getResponse().getHeader("Strict-Transport-Security").contains("max-age"));
        
        assertNotNull(result.getResponse().getHeader("Content-Security-Policy"));
    }

    @Test
    void actuatorEndpoints_ShouldBeAccessibleFromLocalhost() throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isOk());
    }

    @Test
    void corsHeaders_ShouldBePresent() throws Exception {
        JobMatchRequest request = new JobMatchRequest();
        request.setUserProfile("Experienced Java developer with Spring Boot knowledge");

        MvcResult result = mockMvc.perform(options("/api/v1/vectors/jobs/match")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andReturn();

        assertNotNull(result.getResponse().getHeader("Access-Control-Allow-Origin"));
    }
}

