package com.stocksim.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.math.BigDecimal;

@Document(collection = "stocks")
public class Stock {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String symbol;
    
    private String name;
    private BigDecimal currentPrice;
    private Long volume;
    
    // Constructors
    public Stock() {
    }
    
    public Stock(String symbol, String name, BigDecimal currentPrice, Long volume) {
        this.symbol = symbol;
        this.name = name;
        this.currentPrice = currentPrice;
        this.volume = volume;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
    
    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
    
    public Long getVolume() {
        return volume;
    }
    
    public void setVolume(Long volume) {
        this.volume = volume;
    }
} 