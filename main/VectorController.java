import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/vectors")
public class VectorController {
    private static final Logger logger = LoggerFactory.getLogger(VectorController.class);
    
    private final CreateEmbeddings jobPostService;

    
    public VectorController(CreateEmbeddings jobPostService) {
        this.jobPostService = jobPostService;
    }

    @GetMapping("/generate")
    public ResponseEntity<String> generateEmbeddings() {
        try {
            jobPostService.createEmbeddings();
            logger.info("Embeddings generated successfully");
            return ResponseEntity.ok("Embeddings generated and saved successfully!");
        } catch (Exception e) {
            logger.error("Failed to generate embeddings", e);
            return ResponseEntity.internalServerError()
                .body("Failed to generate embeddings: " + e.getMessage());
        }
    }

    @Operation(summary = "Find matching jobs", description = "Returns jobs matching the user profile")
    @ApiResponse(responseCode = "200", description = "Successfully found matching jobs")
    @PostMapping("/jobs/match")
    public ResponseEntity<List<JobMatch>> findMatchingJobs(@RequestBody String userProfile) {
        try {
            List<JobMatch> matches = jobMatchingService.findMatchingJobs(userProfile);
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
