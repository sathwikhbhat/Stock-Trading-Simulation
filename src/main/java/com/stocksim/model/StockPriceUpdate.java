package com.stocksim.model;

import java.math.BigDecimal;

public class StockPriceUpdate {
    private String id;
    private String symbol;
    private BigDecimal currentPrice;
    private BigDecimal priceChange;
    private Long volume;

    public StockPriceUpdate() {
    }

    public StockPriceUpdate(String id, String symbol, BigDecimal currentPrice, BigDecimal priceChange, Long volume) {
        this.id = id;
        this.symbol = symbol;
        this.currentPrice = currentPrice;
        this.priceChange = priceChange;
        this.volume = volume;
    }

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

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getPriceChange() {
        return priceChange;
    }

    public void setPriceChange(BigDecimal priceChange) {
        this.priceChange = priceChange;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }
} 