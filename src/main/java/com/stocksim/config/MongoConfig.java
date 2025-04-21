package com.stocksim.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableMongoRepositories(basePackages = "com.stocksim.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.database}")
    private String databaseName;
    
    @Value("${spring.data.mongodb.host}")
    private String host;
    
    @Value("${spring.data.mongodb.port}")
    private String port;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Override
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString("mongodb://" + host + ":" + port + "/" + databaseName);
        
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .applyToSocketSettings(builder -> 
                builder.connectTimeout(5000, TimeUnit.MILLISECONDS)
                       .readTimeout(5000, TimeUnit.MILLISECONDS))
            .applyToConnectionPoolSettings(builder ->
                builder.maxSize(50)
                       .minSize(10)
                       .maxWaitTime(5000, TimeUnit.MILLISECONDS))
            .build();
        
        logger.info("Configuring MongoDB connection to {}:{}/{}", host, port, databaseName);
        return MongoClients.create(mongoClientSettings);
    }
} 