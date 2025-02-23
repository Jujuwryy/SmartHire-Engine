import org.bson.BsonArray;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertManyResult;
import com.george.model.Post;
import com.george.model.PostRepository;
import java.util.ArrayList;
import java.util.List;

@Service
public class CreateEmbeddings {

    @Autowired
    private VectorEmbeddings embeddingProvider;
    
    @Autowired
    private PostRepository repo;

    public void createEmbeddings() {
        String uri = System.getenv("ATLAS_CONNECTION_STRING");
        if (uri == null || uri.isEmpty()) {
            throw new RuntimeException("ATLAS_CONNECTION_STRING env variable is not set or is empty.");
        }

        // Fetch existing posts from repository
        List<Post> existingPosts = repo.findAll();
        
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("sample_db");
            MongoCollection<Document> collection = database.getCollection("JobPost");

            System.out.println("Creating embeddings for " + existingPosts.size() + " posts");

            // Convert Posts to Documents and extract descriptions
            List<Document> documents = new ArrayList<>();
            List<String> descriptions = new ArrayList<>();
            
            for (Post post : existingPosts) {
                // Convert requiredTechs to ArrayList to ensure proper MongoDB serialization
                ArrayList<String> techsList = new ArrayList<>(post.getRequiredTechs());
                
                Document doc = new Document()
                    .append("jobTitle", post.getJobTitle())
                    .append("jobDescription", post.getJobDescription())
                    .append("experience", post.getExperience())
                    .append("requiredTechs", techsList);
                
                documents.add(doc);
                descriptions.add(post.getJobDescription());
            }

            // Generate embeddings
            List<BsonArray> embeddings = embeddingProvider.getEmbeddings(descriptions);

            // Add embeddings to documents
            for (int i = 0; i < documents.size(); i++) {
                documents.get(i).append("embedding", embeddings.get(i));
            }

            // Insert documents with embeddings
            try {
                InsertManyResult result = collection.insertMany(documents);
                List<String> insertedIds = new ArrayList<>();
                result.getInsertedIds().values()
                    .forEach(doc -> insertedIds.add(doc.toString()));
                System.out.println("Inserted " + insertedIds.size() + " documents with embeddings to " + 
                    collection.getNamespace() + " collection: \n " + insertedIds);
            } catch (MongoException me) {
                System.err.println("Failed to insert documents: " + me.getMessage());
                throw new RuntimeException("Failed to insert documents", me);
            }
        } catch (MongoException me) {
            System.err.println("Failed to connect to MongoDB: " + me.getMessage());
            throw new RuntimeException("Failed to connect to MongoDB ", me);
        } catch (Exception e) {
            System.err.println("Operation failed: " + e.getMessage());
            throw new RuntimeException("Operation failed: ", e);
        }
    }
}
