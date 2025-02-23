import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "sample_db";
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(System.getenv("ATLAS_CONNECTION_STRING"));
    }

    @Override
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(new DoubleToBsonConverter()));
    }
}
