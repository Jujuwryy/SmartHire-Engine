import com.george.Vector.VectorEmbeddings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonArray;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import com.george.model.Post;
import com.george.model.JobMatch;

@Service
public class JobMatchingService {

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private VectorEmbeddings vectorEmbeddings;

    /**
     * Finds matching jobs based on user profile using vector similarity search
     * 
     * @param userProfile Text description of user's skills and preferences
     * @return List of matching jobs with similarity scores
     */
    public List<JobMatch> findMatchingJobs(String userProfile) {
        try {
            // Generate embedding for user profile
            BsonArray userEmbedding = vectorEmbeddings.getEmbedding(userProfile);

            MongoDatabase database = mongoClient.getDatabase("sample_db");
            MongoCollection<Document> collection = database.getCollection("JobPost");

            // Create aggregation pipeline for vector search
            List<Document> pipeline = Arrays.asList(
                new Document("$search", new Document()
                    .append("index", "vector_index")
                    .append("knnBeta", new Document()
                        .append("vector", userEmbedding)
                        .append("path", "embedding")
                        .append("k", 10))),
                
                new Document("$project", new Document()
                    .append("jobTitle", 1)
                    .append("jobDescription", 1)
                    .append("experience", 1)
                    .append("requiredTechs", 1)
                    .append("score", new Document("$meta", "searchScore")))
            );

            // Execute search and convert results
            List<JobMatch> matches = new ArrayList<>();
            collection.aggregate(pipeline)
                .forEach(doc -> {
                    JobMatch match = new JobMatch();
                    match.setJob(convertDocumentToPost(doc));
                    match.setConfidence(doc.getDouble("score"));
                    match.setMatchReasons(generateMatchReasons(doc, userProfile));
                    matches.add(match);
                    
                });

            return matches;
        } catch (Exception e) {
            System.err.println("Error in vector search: " + e.getMessage());
            throw new RuntimeException("Failed to perform vector search", e);
        }
    }

    private Post convertDocumentToPost(Document doc) {
        Post post = new Post();
        post.setId(doc.getObjectId("_id").toString());
        post.setJobTitle(doc.getString("jobTitle"));
        post.setJobDescription(doc.getString("jobDescription"));
        post.setExperience(doc.getInteger("experience"));
        post.setRequiredTechs(doc.getList("requiredTechs", String.class));
        return post;
    }

    private List<String> generateMatchReasons(Document doc, String userProfile) {
        List<String> reasons = new ArrayList<>();
        
        // Add match reasons based on similarity score
        double score = doc.getDouble("score");
        if (score > 0.8) {
            reasons.add("Very strong overall match");
        } else if (score > 0.6) {
            reasons.add("Good overall match");
        }
        
        // Add specific reasons based on required technologies
        List<String> techs = doc.getList("requiredTechs", String.class);
        if (techs != null) {
            for (String tech : techs) {
                if (userProfile.toLowerCase().contains(tech.toLowerCase())) {
                    reasons.add("Matching skill: " + tech);
                }
            }
        }

        return reasons;
    }
}
