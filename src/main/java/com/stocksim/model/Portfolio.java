package com.stocksim.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import java.math.BigDecimal;

@Document(collection = "portfolios")
public class Portfolio {
    
    @Id
    private String id;
    
    @DBRef
    private User user;
    
    @DBRef
    private Stock stock;
    
    private int quantity;
    private BigDecimal purchasePrice;
    
    // Constructors
    public Portfolio() {
    }
    
    public Portfolio(User user, Stock stock, int quantity, BigDecimal purchasePrice) {
        this.user = user;
        this.stock = stock;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Stock getStock() {
        return stock;
    }
    
    public void setStock(Stock stock) {
        this.stock = stock;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }
    
    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
    
    // Helper methods
    public BigDecimal getCurrentValue() {
        return stock.getCurrentPrice().multiply(new BigDecimal(quantity));
    }
    
    public BigDecimal getProfitLoss() {
        BigDecimal currentValue = getCurrentValue();
        BigDecimal investmentValue = purchasePrice.multiply(new BigDecimal(quantity));
        return currentValue.subtract(investmentValue);
    }
} 