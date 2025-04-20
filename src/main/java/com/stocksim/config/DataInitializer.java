package com.stocksim.config;

import com.stocksim.service.StockService;
import com.stocksim.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2) // Run after MongoHealthCheck
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final StockService stockService;
    private final UserService userService;

    @Autowired
    public DataInitializer(StockService stockService, UserService userService) {
        this.stockService = stockService;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        try {
            // Initialize sample stock data
            logger.info("Starting data initialization...");
            stockService.initializeStocks();
            
            // Create a demo user if none exists
            if (userService.getAllUsers().isEmpty()) {
                logger.info("Creating demo user...");
                userService.registerUser("demo", "password", "demo@example.com");
                logger.info("Demo user created successfully!");
            }
            
            logger.info("Data initialization completed successfully!");
        } catch (Exception e) {
            logger.error("Error during data initialization", e);
        }
    }
} 