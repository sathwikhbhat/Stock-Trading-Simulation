package com.stocksim.controller;

import com.stocksim.model.Stock;
import com.stocksim.model.StockPriceUpdate;
import com.stocksim.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class StockWebSocketController {

    private final StockService stockService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public StockWebSocketController(StockService stockService, SimpMessagingTemplate messagingTemplate) {
        this.stockService = stockService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/fetch-stocks")
    @SendTo("/topic/stock-updates")
    public List<StockPriceUpdate> fetchStocks() {
        List<Stock> stocks = stockService.getAllStocks();
        List<StockPriceUpdate> updates = new ArrayList<>();
        
        for (Stock stock : stocks) {
            StockPriceUpdate update = new StockPriceUpdate(
                stock.getId(),
                stock.getSymbol(),
                stock.getCurrentPrice(),
                BigDecimal.ZERO, // No change initially
                stock.getVolume()
            );
            updates.add(update);
        }
        
        return updates;
    }

    @MessageMapping("/ping")
    @SendTo("/topic/pong")
    public Map<String, Object> handlePing() {
        Map<String, Object> response = new HashMap<>();
        response.put("action", "pong");
        response.put("timestamp", System.currentTimeMillis());
        response.put("serverTime", new java.util.Date().toString());
        return response;
    }

    // This method will be called from the scheduled task
    public void sendStockPriceUpdates() {
        List<StockPriceUpdate> updates = stockService.updateStockPrices();
        messagingTemplate.convertAndSend("/topic/stock-updates", updates);
    }
} 