package com.stocksim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StockSimulationApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockSimulationApplication.class, args);
    }
} 