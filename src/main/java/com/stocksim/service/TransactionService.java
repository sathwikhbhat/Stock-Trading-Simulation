package com.stocksim.service;

import com.stocksim.model.Stock;
import com.stocksim.model.Transaction;
import com.stocksim.model.User;
import com.stocksim.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final PortfolioService portfolioService;
    
    @Autowired
    public TransactionService(TransactionRepository transactionRepository, 
                              UserService userService,
                              PortfolioService portfolioService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.portfolioService = portfolioService;
    }
    
    public List<Transaction> getTransactionsByUser(User user) {
        return transactionRepository.findByUserOrderByTimestampDesc(user);
    }
    
    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
    
    public boolean buyStock(User user, Stock stock, int quantity) {
        BigDecimal totalCost = stock.getCurrentPrice().multiply(new BigDecimal(quantity));
        
        // Check if user has enough balance
        if (user.getAccountBalance().compareTo(totalCost) < 0) {
            return false; // Insufficient funds
        }
        
        // Create transaction record
        Transaction transaction = new Transaction(user, stock, "BUY", quantity, stock.getCurrentPrice());
        transactionRepository.save(transaction);
        
        // Update user's account balance
        userService.updateAccountBalance(user, totalCost.negate());
        
        // Update user's portfolio
        portfolioService.updatePortfolio(user, stock, quantity, stock.getCurrentPrice());
        
        return true;
    }
    
    public boolean sellStock(User user, Stock stock, int quantity) {
        // Check if user has enough of the stock
        var portfolioOpt = portfolioService.getPortfolioByUserAndStock(user, stock);
        if (portfolioOpt.isEmpty() || portfolioOpt.get().getQuantity() < quantity) {
            return false; // Not enough stocks
        }
        
        BigDecimal totalValue = stock.getCurrentPrice().multiply(new BigDecimal(quantity));
        
        // Create transaction record
        Transaction transaction = new Transaction(user, stock, "SELL", quantity, stock.getCurrentPrice());
        transactionRepository.save(transaction);
        
        // Update user's account balance
        userService.updateAccountBalance(user, totalValue);
        
        // Update user's portfolio
        portfolioService.updatePortfolio(user, stock, -quantity, stock.getCurrentPrice());
        
        return true;
    }
} 