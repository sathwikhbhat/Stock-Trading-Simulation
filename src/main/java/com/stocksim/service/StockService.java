package com.stocksim.service;

import com.stocksim.model.Stock;
import com.stocksim.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class StockService {
    
    private final StockRepository stockRepository;
    private final Random random = new Random();
    
    @Autowired
    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }
    
    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }
    
    public Optional<Stock> getStockById(String id) {
        return stockRepository.findById(id);
    }
    
    public Optional<Stock> getStockBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol);
    }
    
    public Stock saveStock(Stock stock) {
        return stockRepository.save(stock);
    }
    
    // Simulate price changes (in a real app, this would use external market data)
    public void updateStockPrices() {
        List<Stock> stocks = stockRepository.findAll();
        
        for (Stock stock : stocks) {
            // Generate a random percentage change between -5% and +5%
            double percentChange = (random.nextDouble() - 0.5) * 0.1; // -5% to +5%
            
            BigDecimal currentPrice = stock.getCurrentPrice();
            BigDecimal newPrice = currentPrice.multiply(BigDecimal.ONE.add(new BigDecimal(percentChange)));
            
            // Round to 2 decimal places
            newPrice = newPrice.setScale(2, RoundingMode.HALF_UP);
            
            // Update the stock price
            stock.setCurrentPrice(newPrice);
            
            // Update the volume (random increase between 0 and 10000)
            long volumeChange = random.nextInt(10000);
            stock.setVolume(stock.getVolume() + volumeChange);
            
            stockRepository.save(stock);
        }
    }
    
    // Initialize with some sample stocks
    public void initializeStocks() {
        if (stockRepository.count() == 0) {
            // Add sample stocks
            stockRepository.save(new Stock("AAPL", "Apple Inc.", new BigDecimal("150.50"), 1000000L));
            stockRepository.save(new Stock("MSFT", "Microsoft Corporation", new BigDecimal("310.25"), 800000L));
            stockRepository.save(new Stock("GOOGL", "Alphabet Inc.", new BigDecimal("135.75"), 650000L));
            stockRepository.save(new Stock("AMZN", "Amazon.com Inc.", new BigDecimal("130.00"), 750000L));
            stockRepository.save(new Stock("TSLA", "Tesla, Inc.", new BigDecimal("250.30"), 900000L));
        }
    }
} 