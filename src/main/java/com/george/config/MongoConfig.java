package com.george.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MongoConfig.class);

    private final String databaseName;
    private final String connectionString;

    public MongoConfig(@Value("${app.mongodb.database-name:jobs_db}") String databaseName,
                       @Value("${spring.data.mongodb.uri}") String connectionString) {
        this.databaseName = databaseName;
        this.connectionString = connectionString;
    }

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        // Connection string is validated at startup by ConfigurationValidator
        logger.info("Creating MongoDB client connection");
        return MongoClients.create(connectionString);
    }

    @Override
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(new DoubleToBsonConverter()));
    }
}
