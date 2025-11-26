package com.george.controller;

import com.george.CreateEmbeddings;
import com.george.config.AppProperties;
import com.george.dto.JobMatchRequest;
import com.george.dto.JobMatchResponse;
import com.george.model.JobMatch;
import com.george.service.JobMatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${app.api.base-path:/api/v1}/vectors")
@Tag(name = "Vector Embeddings", description = "API for managing vector embeddings and job matching")
public class VectorController {
    
    private final CreateEmbeddings createEmbeddingsService;
    private final JobMatchingService jobMatchingService;
    private final AppProperties appProperties;

    public VectorController(CreateEmbeddings createEmbeddingsService,
                            JobMatchingService jobMatchingService,
                            AppProperties appProperties) {
        this.createEmbeddingsService = createEmbeddingsService;
        this.jobMatchingService = jobMatchingService;
        this.appProperties = appProperties;
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
        createEmbeddingsService.createEmbeddings();
        return ResponseEntity.ok("Embeddings generated and saved successfully!");
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
        List<JobMatch> matches = jobMatchingService.findMatchingJobs(request);

        JobMatchResponse response = new JobMatchResponse(matches);
        response.setQueryId(queryId);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Find matching jobs (simple)",
        description = "Simplified endpoint that accepts a plain text user profile string"
    )
    @PostMapping("/jobs/match/simple")
    public ResponseEntity<JobMatchResponse> findMatchingJobsSimple(
            @RequestBody String userProfile) {
        
        JobMatchRequest request = new JobMatchRequest(userProfile);
        request.setLimit(appProperties.getMatching().getDefaultLimit());
        request.setMinConfidence(appProperties.getMatching().getDefaultMinConfidence());
        
        return findMatchingJobs(request);
    }
}
