package com.stocksim.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1) // Run before other CommandLineRunners
public class MongoHealthCheck implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MongoHealthCheck.class);
    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoHealthCheck(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            // Test MongoDB connectivity
            mongoTemplate.getDb().getName();
            logger.info("MongoDB connection successful!");
        } catch (Exception e) {
            logger.error("MongoDB connection failed! Please ensure MongoDB is running.", e);
            
            // Print user-friendly message
            System.err.println("\n");
            System.err.println("===========================================================");
            System.err.println("=  ERROR: Cannot connect to MongoDB                       =");
            System.err.println("=  Please ensure MongoDB is installed and running         =");
            System.err.println("=  The application requires MongoDB to be running         =");
            System.err.println("=  on localhost:27017                                     =");
            System.err.println("===========================================================");
            System.err.println("\n");
        }
    }
} 