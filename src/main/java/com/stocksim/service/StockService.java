package com.stocksim.service;

import com.stocksim.model.Stock;
import com.stocksim.model.Stock.PricePoint;
import com.stocksim.model.StockPriceUpdate;
import com.stocksim.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
            
            // Generate a random percentage change between -3% and +3%
            double percentChange = (random.nextDouble() - 0.5) * 0.06; // -3% to +3%
            
            BigDecimal newPrice = oldPrice.multiply(BigDecimal.ONE.add(new BigDecimal(percentChange)));
            
            // Round to 2 decimal places
            newPrice = newPrice.setScale(2, RoundingMode.HALF_UP);
            
            // Calculate price change (absolute value)
            BigDecimal priceChange = newPrice.subtract(oldPrice);
            
            // Update the stock price
            stock.setCurrentPrice(newPrice);
            
            // Update the volume (random increase between 0 and 5000)
            long volumeChange = random.nextInt(5000);
            stock.setVolume(stock.getVolume() + volumeChange);
            
            // Add this price point to history
            addPriceToHistory(stock, newPrice);
            
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
    
    // Add current price to history - FIXED to prevent duplicate timestamps and ensure correct time ordering
    private void addPriceToHistory(Stock stock, BigDecimal price) {
        // Initialize history list if null
        if (stock.getPriceHistory() == null) {
            stock.setPriceHistory(new ArrayList<>());
        }
        
        List<PricePoint> history = stock.getPriceHistory();
        
        // Get current time
        LocalDateTime now = LocalDateTime.now();
        long currentTimestamp = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        // Check if we already have a point at this timestamp (avoid duplicates)
        boolean hasExistingPoint = false;
        for (PricePoint point : history) {
            // If timestamps are within 1 second of each other, consider them duplicates
            if (Math.abs(point.getTimestamp() - currentTimestamp) < 1000) {
                hasExistingPoint = true;
                // Update the existing point with the new price
                point.setPrice(price);
                logger.debug("Updated existing price point for {} at {} with price ${}", 
                    stock.getSymbol(), 
                    new java.util.Date(point.getTimestamp()),
                    price);
                break;
            }
        }
        
        // If no existing point, add a new one
        if (!hasExistingPoint) {
            // Only keep up to 30 data points
            if (history.size() >= 30) {
                history.remove(0);  // Remove oldest point
            }
            
            // Add new price point with current timestamp
            PricePoint newPoint = new PricePoint(currentTimestamp, price);
            history.add(newPoint);
            
            logger.debug("Added new price point for {} at {} with price ${}", 
                stock.getSymbol(),
                new java.util.Date(currentTimestamp),
                price);
        }
    }
    
    // Generate historical price data (30 days)
    private void generatePriceHistory(Stock stock) {
        List<PricePoint> history = new ArrayList<>();
        BigDecimal basePrice = stock.getCurrentPrice();
        
        // Clear any existing price history first
        stock.setPriceHistory(new ArrayList<>());
        
        // Generate one data point per day for the past 30 days
        LocalDate today = LocalDate.now();
        
        logger.info("Generating completely new price history for {} from {} to {}", 
            stock.getSymbol(), 
            today.minusDays(30),
            today);
        
        // Start with a base price in the right range for each stock
        switch(stock.getSymbol()) {
            case "AAPL": basePrice = new BigDecimal("145.00"); break;
            case "MSFT": basePrice = new BigDecimal("300.00"); break;
            case "GOOGL": basePrice = new BigDecimal("125.00"); break;
            case "AMZN": basePrice = new BigDecimal("120.00"); break;
            case "TSLA": basePrice = new BigDecimal("240.00"); break;
            default: basePrice = new BigDecimal("100.00");
        }
        
        // Generate data for each day (including weekends for simplicity)
        for (int i = 30; i >= 0; i--) {
            // Create date for this data point (days ago)
            LocalDate date = today.minusDays(i);
            
            // Set a consistent time for market close
            LocalDateTime dateTime = date.atTime(16, 0); // 4:00 PM market close
            long timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            
            // Base volatility on the stock symbol to simulate different patterns
            double volatility = 0.02;  // Default 2% daily variation
            if (stock.getSymbol().equals("TSLA")) volatility = 0.05;  // Tesla more volatile
            if (stock.getSymbol().equals("MSFT")) volatility = 0.015; // Microsoft less volatile
            
            // Generate a trend with some randomness
            double trend = 1.0 + ((random.nextDouble() - 0.5) * volatility * 2);
            
            // Adjust the base price by the trend and round to 2 decimal places
            BigDecimal adjustedPrice = basePrice.multiply(new BigDecimal(trend))
                                             .setScale(2, RoundingMode.HALF_UP);
            
            // Set this as the new base price for the next iteration
            basePrice = adjustedPrice;
            
            // Create new price point
            PricePoint pricePoint = new PricePoint(timestamp, adjustedPrice);
            
            // Add to history
            history.add(pricePoint);
            
            logger.debug("Added historical price point: {} - ${} on {}", 
                stock.getSymbol(),
                adjustedPrice, 
                dateTime.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        // Update stock with new price history
        stock.setPriceHistory(history);
        
        // Update current price to match the last historical price
        stock.setCurrentPrice(history.get(history.size() - 1).getPrice());
        
        logger.info("Generated {} price history points for {} spanning 31 days", 
            history.size(), 
            stock.getSymbol());
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
            
            // Generate price history for each stock
            generatePriceHistory(apple);
            generatePriceHistory(microsoft);
            generatePriceHistory(google);
            generatePriceHistory(amazon);
            generatePriceHistory(tesla);
            
            // Save all stocks
            stockRepository.save(apple);
            stockRepository.save(microsoft);
            stockRepository.save(google);
            stockRepository.save(amazon);
            stockRepository.save(tesla);
            
            logger.info("Sample stock data initialized successfully!");
        } else {
            logger.info("Stocks already exist in the database. Skipping initialization.");
        }
    }
} 