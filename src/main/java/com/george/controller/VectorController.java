package com.george.controller;

import com.george.service.CreateEmbeddings;
import com.george.dto.JobMatchRequest;
import com.george.dto.JobMatchResponse;
import com.george.model.JobMatch;
import com.george.service.ExportService;
import com.george.service.JobMatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${app.api.base-path:/api/v1}/vectors")
@Tag(name = "Vector Embeddings", description = "API for managing vector embeddings and job matching")
public class VectorController {
    
    private final CreateEmbeddings createEmbeddingsService;
    private final JobMatchingService jobMatchingService;
    private final ExportService exportService;

    public VectorController(CreateEmbeddings createEmbeddingsService,
                            JobMatchingService jobMatchingService,
                            ExportService exportService) {
        this.createEmbeddingsService = createEmbeddingsService;
        this.jobMatchingService = jobMatchingService;
        this.exportService = exportService;
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
        
        List<JobMatch> matches = jobMatchingService.findMatchingJobs(request);
        JobMatchResponse response = new JobMatchResponse(matches);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Find matching jobs (simple)",
        description = "Simplified endpoint that accepts a plain text user profile string"
    )
    @PostMapping("/jobs/match/simple")
    public ResponseEntity<JobMatchResponse> findMatchingJobsSimple(
            @RequestBody String userProfile) {
        
        List<JobMatch> matches = jobMatchingService.findMatchingJobsSimple(userProfile);
        JobMatchResponse response = new JobMatchResponse(matches);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Export matching jobs",
        description = "Finds matching jobs and exports them in the specified format (CSV or JSON). Default format is JSON."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully exported job matches",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters or format",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error during export",
            content = @Content
        )
    })
    @PostMapping("/jobs/match/export")
    public ResponseEntity<String> exportMatchingJobs(
            @Valid @RequestBody JobMatchRequest request,
            @RequestParam(defaultValue = "json") String format) {
        
        if (format != null && !format.equalsIgnoreCase("json") && !format.equalsIgnoreCase("csv")) {
            return ResponseEntity.badRequest()
                    .body("Invalid format. Supported formats: json, csv");
        }
        
        List<JobMatch> matches = jobMatchingService.findMatchingJobs(request);
        JobMatchResponse response = new JobMatchResponse(matches);
        
        String exportData;
        String contentType;
        String filename;
        
        if ("csv".equalsIgnoreCase(format)) {
            exportData = exportService.exportToCsv(response);
            contentType = "text/csv";
            filename = "job_matches.csv";
        } else {
            exportData = exportService.exportToJson(response);
            contentType = MediaType.APPLICATION_JSON_VALUE;
            filename = "job_matches.json";
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
    }

    @Operation(
        summary = "Export matching jobs (simple)",
        description = "Finds matching jobs from a plain text user profile and exports them in the specified format (CSV or JSON). Default format is JSON."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully exported job matches",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters or format",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error during export",
            content = @Content
        )
    })
    @PostMapping("/jobs/match/simple/export")
    public ResponseEntity<String> exportMatchingJobsSimple(
            @RequestBody String userProfile,
            @RequestParam(defaultValue = "json") String format) {
        
        if (format != null && !format.equalsIgnoreCase("json") && !format.equalsIgnoreCase("csv")) {
            return ResponseEntity.badRequest()
                    .body("Invalid format. Supported formats: json, csv");
        }
        
        List<JobMatch> matches = jobMatchingService.findMatchingJobsSimple(userProfile);
        JobMatchResponse response = new JobMatchResponse(matches);
        
        String exportData;
        String contentType;
        String filename;
        
        if ("csv".equalsIgnoreCase(format)) {
            exportData = exportService.exportToCsv(response);
            contentType = "text/csv";
            filename = "job_matches.csv";
        } else {
            exportData = exportService.exportToJson(response);
            contentType = MediaType.APPLICATION_JSON_VALUE;
            filename = "job_matches.json";
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
    }
}
