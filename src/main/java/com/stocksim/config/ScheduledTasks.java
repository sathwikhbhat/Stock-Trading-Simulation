package com.stocksim.config;

import com.stocksim.controller.StockWebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class ScheduledTasks {

    private final StockWebSocketController stockWebSocketController;

    @Autowired
    public ScheduledTasks(StockWebSocketController stockWebSocketController) {
        this.stockWebSocketController = stockWebSocketController;
    }

    // Update prices every 5 seconds
    @Scheduled(fixedRate = 5000)
    public void updateStockPrices() {
        stockWebSocketController.sendStockPriceUpdates();
    }
} 