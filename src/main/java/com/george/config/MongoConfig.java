package com.george.config;

import com.george.util.Constants;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MongoConfig.class);

    @Override
    protected String getDatabaseName() {
        return Constants.DATABASE_NAME;
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        String connectionString = System.getenv(Constants.ENV_ATLAS_CONNECTION_STRING);
        if (connectionString == null || connectionString.isEmpty()) {
            logger.error("MongoDB connection string is not set");
            throw new IllegalStateException(Constants.ERROR_MISSING_CONNECTION_STRING);
        }
        logger.info("Creating MongoDB client connection");
        return MongoClients.create(connectionString);
    }

    @Override
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(new DoubleToBsonConverter()));
    }
}
