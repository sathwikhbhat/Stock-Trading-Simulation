package com.stocksim.service;

import com.stocksim.model.Stock;
import com.stocksim.model.StockPriceUpdate;
import com.stocksim.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class StockService {
    
    private static final Logger logger = LoggerFactory.getLogger(StockService.class);
    private final StockRepository stockRepository;
    private final Random random = new Random();
    
    @Autowired
    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }
    
    public List<Stock> getAllStocks() {
        List<Stock> stocks = stockRepository.findAll();
        logger.info("Fetched {} stocks from database", stocks.size());
        return stocks;
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
    
    // Simulate price changes and return update information for WebSocket
    public List<StockPriceUpdate> updateStockPrices() {
        List<Stock> stocks = stockRepository.findAll();
        logger.debug("Updating prices for {} stocks", stocks.size());
        List<StockPriceUpdate> updates = new ArrayList<>();
        
        for (Stock stock : stocks) {
            // Save the old price to calculate change
            BigDecimal oldPrice = stock.getCurrentPrice();
            
            // Generate a random percentage change between -5% and +5%
            double percentChange = (random.nextDouble() - 0.5) * 0.1; // -5% to +5%
            
            BigDecimal newPrice = oldPrice.multiply(BigDecimal.ONE.add(new BigDecimal(percentChange)));
            
            // Round to 2 decimal places
            newPrice = newPrice.setScale(2, RoundingMode.HALF_UP);
            
            // Calculate price change (absolute value)
            BigDecimal priceChange = newPrice.subtract(oldPrice);
            
            // Update the stock price
            stock.setCurrentPrice(newPrice);
            
            // Update the volume (random increase between 0 and 10000)
            long volumeChange = random.nextInt(10000);
            stock.setVolume(stock.getVolume() + volumeChange);
            
            // Save to database
            stockRepository.save(stock);
            
            // Create and add update message
            StockPriceUpdate update = new StockPriceUpdate(
                stock.getId(),
                stock.getSymbol(),
                newPrice,
                priceChange,
                stock.getVolume()
            );
            updates.add(update);
        }
        
        logger.debug("Price updates completed, returning {} updates", updates.size());
        return updates;
    }
    
    // Initialize with some sample stocks
    public void initializeStocks() {
        long count = stockRepository.count();
        logger.info("Current stock count in database: {}", count);
        
        if (count == 0) {
            logger.info("No stocks found. Initializing sample stock data...");
            
            // Add sample stocks
            Stock apple = new Stock("AAPL", "Apple Inc.", new BigDecimal("150.50"), 1000000L);
            Stock microsoft = new Stock("MSFT", "Microsoft Corporation", new BigDecimal("310.25"), 800000L);
            Stock google = new Stock("GOOGL", "Alphabet Inc.", new BigDecimal("135.75"), 650000L);
            Stock amazon = new Stock("AMZN", "Amazon.com Inc.", new BigDecimal("130.00"), 750000L);
            Stock tesla = new Stock("TSLA", "Tesla, Inc.", new BigDecimal("250.30"), 900000L);
            
            // Save all stocks
            stockRepository.save(apple);
            stockRepository.save(microsoft);
            stockRepository.save(google);
            stockRepository.save(amazon);
            stockRepository.save(tesla);
            
            logger.info("Sample stock data initialized successfully!");
        } else {
            logger.info("Stocks already initialized. Skipping initialization.");
        }
    }
} 