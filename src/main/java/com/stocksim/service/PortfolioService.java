package com.stocksim.service;

import com.stocksim.model.Portfolio;
import com.stocksim.model.Stock;
import com.stocksim.model.User;
import com.stocksim.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PortfolioService {
    
    private final PortfolioRepository portfolioRepository;
    
    @Autowired
    public PortfolioService(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }
    
    public List<Portfolio> getPortfolioByUser(User user) {
        return portfolioRepository.findByUser(user);
    }
    
    public Optional<Portfolio> getPortfolioById(String id) {
        return portfolioRepository.findById(id);
    }
    
    public Optional<Portfolio> getPortfolioByUserAndStock(User user, Stock stock) {
        return portfolioRepository.findByUserAndStock(user, stock);
    }
    
    public Portfolio savePortfolio(Portfolio portfolio) {
        return portfolioRepository.save(portfolio);
    }
    
    public void updatePortfolio(User user, Stock stock, int quantity, BigDecimal price) {
        Optional<Portfolio> portfolioOpt = portfolioRepository.findByUserAndStock(user, stock);
        
        if (portfolioOpt.isPresent()) {
            // Update existing portfolio entry
            Portfolio portfolio = portfolioOpt.get();
            int newQuantity = portfolio.getQuantity() + quantity;
            
            if (newQuantity <= 0) {
                // If quantity becomes 0 or negative, remove the portfolio entry
                portfolioRepository.delete(portfolio);
            } else {
                // Otherwise update the quantity and calculated purchase price
                portfolio.setQuantity(newQuantity);
                
                // Calculate new average purchase price
                BigDecimal totalOldValue = portfolio.getPurchasePrice().multiply(new BigDecimal(portfolio.getQuantity()));
                BigDecimal totalNewValue = price.multiply(new BigDecimal(quantity));
                BigDecimal totalValue = totalOldValue.add(totalNewValue);
                BigDecimal newAveragePrice = totalValue.divide(new BigDecimal(newQuantity), 2, BigDecimal.ROUND_HALF_UP);
                
                portfolio.setPurchasePrice(newAveragePrice);
                portfolioRepository.save(portfolio);
            }
        } else if (quantity > 0) {
            // Create new portfolio entry
            Portfolio portfolio = new Portfolio(user, stock, quantity, price);
            portfolioRepository.save(portfolio);
        }
    }
    
    public BigDecimal calculateTotalPortfolioValue(User user) {
        List<Portfolio> portfolioItems = portfolioRepository.findByUser(user);
        BigDecimal totalValue = BigDecimal.ZERO;
        
        for (Portfolio item : portfolioItems) {
            totalValue = totalValue.add(item.getCurrentValue());
        }
        
        return totalValue;
    }
    
    public BigDecimal calculateTotalProfitLoss(User user) {
        List<Portfolio> portfolioItems = portfolioRepository.findByUser(user);
        BigDecimal totalProfitLoss = BigDecimal.ZERO;
        
        for (Portfolio item : portfolioItems) {
            totalProfitLoss = totalProfitLoss.add(item.getProfitLoss());
        }
        
        return totalProfitLoss;
    }
} 