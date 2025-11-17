package com.george.controller;

import com.george.CreateEmbeddings;
import com.george.dto.JobMatchRequest;
import com.george.dto.JobMatchResponse;
import com.george.model.JobMatch;
import com.george.service.JobMatchingService;
import com.george.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(Constants.VECTORS_ENDPOINT)
@Tag(name = "Vector Embeddings", description = "API for managing vector embeddings and job matching")
public class VectorController {
    
    private static final Logger logger = LoggerFactory.getLogger(VectorController.class);
    
    private final CreateEmbeddings createEmbeddingsService;
    private final JobMatchingService jobMatchingService;

    public VectorController(CreateEmbeddings createEmbeddingsService, JobMatchingService jobMatchingService) {
        this.createEmbeddingsService = createEmbeddingsService;
        this.jobMatchingService = jobMatchingService;
    }

    @Operation(
        summary = "Generate embeddings for all job posts",
        description = "Fetches all job posts from the repository, generates vector embeddings using AI, and stores them in MongoDB"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Embeddings generated and saved successfully",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Failed to generate embeddings",
            content = @Content
        )
    })
    @GetMapping("/generate")
    public ResponseEntity<String> generateEmbeddings() {
        logger.info("Received request to generate embeddings");
        try {
            createEmbeddingsService.createEmbeddings();
            logger.info("Embeddings generated successfully");
            return ResponseEntity.ok("Embeddings generated and saved successfully!");
        } catch (Exception e) {
            logger.error("Failed to generate embeddings", e);
            throw e; // Let GlobalExceptionHandler handle it
        }
    }

    @Operation(
        summary = "Find matching jobs",
        description = "Uses vector similarity search to find jobs matching the user profile. Returns ranked results with confidence scores and match reasons."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully found matching jobs",
            content = @Content(schema = @Schema(implementation = JobMatchResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error during job matching",
            content = @Content
        )
    })
    @PostMapping("/jobs/match")
    public ResponseEntity<JobMatchResponse> findMatchingJobs(
            @Valid @RequestBody JobMatchRequest request) {
        
        String queryId = UUID.randomUUID().toString();
        logger.info("Received job matching request [queryId: {}] for profile: {}", 
            queryId, request.getUserProfile().substring(0, Math.min(50, request.getUserProfile().length())));
        
        long startTime = System.currentTimeMillis();
        
        try {
            List<JobMatch> matches = jobMatchingService.findMatchingJobs(request);
            
            long queryTime = System.currentTimeMillis() - startTime;
            
            JobMatchResponse response = new JobMatchResponse(matches);
            response.setQueryTimeMs(queryTime);
            response.setQueryId(queryId);
            
            logger.info("Job matching completed [queryId: {}] - Found {} matches in {}ms", 
                queryId, matches.size(), queryTime);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in job matching [queryId: {}]", queryId, e);
            throw e; // Let GlobalExceptionHandler handle it
        }
    }

    @Operation(
        summary = "Find matching jobs (simple)",
        description = "Simplified endpoint that accepts a plain text user profile string"
    )
    @PostMapping("/jobs/match/simple")
    public ResponseEntity<JobMatchResponse> findMatchingJobsSimple(
            @RequestBody String userProfile) {
        
        logger.info("Received simple job matching request");
        
        JobMatchRequest request = new JobMatchRequest(userProfile);
        request.setLimit(Constants.DEFAULT_MATCH_LIMIT);
        request.setMinConfidence(Constants.DEFAULT_MIN_CONFIDENCE);
        
        return findMatchingJobs(request);
    }
}
