package com.stocksim.config;

import com.stocksim.service.StockService;
import com.stocksim.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final StockService stockService;
    private final UserService userService;
    
    @Autowired
    public DataInitializer(StockService stockService, UserService userService) {
        this.stockService = stockService;
        this.userService = userService;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Initialize sample stocks
        stockService.initializeStocks();
        
        // Create a demo user if no users exist
        if (userService.getAllUsers().isEmpty()) {
            userService.registerUser("demo", "password", "demo@example.com");
        }
    }
} 