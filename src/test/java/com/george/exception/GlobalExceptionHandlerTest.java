package com.george.exception;

import com.george.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GlobalExceptionHandlerTest {

    @Autowired
    private GlobalExceptionHandler exceptionHandler;

    @Test
    void handleEmbeddingException_ReturnsInternalServerError() {
        EmbeddingException ex = new EmbeddingException("Test error");
        ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest());

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleEmbeddingException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Embedding Generation Failed", response.getBody().getError());
    }

    @Test
    void handleJobMatchingException_ReturnsInternalServerError() {
        JobMatchingException ex = new JobMatchingException("Test error");
        ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest());

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleJobMatchingException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Job Matching Failed", response.getBody().getError());
    }

    @Test
    void handleIllegalArgumentException_ReturnsBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest());

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid Argument", response.getBody().getError());
    }
}

