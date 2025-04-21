package com.stocksim.config;

import com.stocksim.service.StockService;
import com.stocksim.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(2) // Run after MongoHealthCheck
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final StockService stockService;
    private final UserService userService;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public DataInitializer(StockService stockService, UserService userService, MongoTemplate mongoTemplate) {
        this.stockService = stockService;
        this.userService = userService;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            String dbName = mongoTemplate.getDb().getName();
            logger.info("Initializing data in database: {}", dbName);
            
            // Removed stock initialization code to prevent sample stock price generation
            
            // Create a demo user if none exists
            if (userService.getAllUsers().isEmpty()) {
                logger.info("No users found. Creating demo user...");
                userService.registerUser("demo", "password", "demo@example.com");
                logger.info("Demo user created successfully!");
            } else {
                logger.info("Users already exist. Skipping user creation.");
            }
            
            logger.info("Data initialization check completed successfully!");
        } catch (Exception e) {
            logger.error("Error during data initialization", e);
        }
    }
} 