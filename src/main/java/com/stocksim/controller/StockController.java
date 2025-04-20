package com.stocksim.controller;

import com.stocksim.model.Stock;
import com.stocksim.model.User;
import com.stocksim.service.StockService;
import com.stocksim.service.TransactionService;
import com.stocksim.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
@RequestMapping("/stocks")
public class StockController {
    
    private static final Logger logger = LoggerFactory.getLogger(StockController.class);
    private final StockService stockService;
    private final UserService userService;
    private final TransactionService transactionService;
    
    @Autowired
    public StockController(StockService stockService, 
                          UserService userService,
                          TransactionService transactionService) {
        this.stockService = stockService;
        this.userService = userService;
        this.transactionService = transactionService;
    }
    
    @GetMapping
    public String getAllStocks(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("stocks", stockService.getAllStocks());
        return "stocks";
    }
    
    @GetMapping("/{id}")
    public String getStockDetails(@PathVariable String id, Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        Optional<Stock> stockOpt = stockService.getStockById(id);
        if (stockOpt.isEmpty()) {
            return "redirect:/stocks";
        }
        
        model.addAttribute("stock", stockOpt.get());
        return "stock-details";
    }
    
    @PostMapping("/buy")
    public String buyStock(@RequestParam String stockId,
                          @RequestParam int quantity,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.getUserById(userId);
        Optional<Stock> stockOpt = stockService.getStockById(stockId);
        
        if (userOpt.isEmpty() || stockOpt.isEmpty() || quantity <= 0) {
            redirectAttributes.addFlashAttribute("error", "Invalid transaction");
            logger.warn("Invalid buy transaction: userId={}, stockId={}, quantity={}", userId, stockId, quantity);
            return "redirect:/";
        }
        
        User user = userOpt.get();
        Stock stock = stockOpt.get();
        boolean success = transactionService.buyStock(user, stock, quantity);
        
        if (!success) {
            redirectAttributes.addFlashAttribute("error", "Insufficient funds to buy " + quantity + " shares of " + stock.getSymbol());
            logger.warn("Insufficient funds: user={}, balance=${}, required=${}", 
                user.getUsername(), 
                user.getAccountBalance(), 
                stock.getCurrentPrice().multiply(new java.math.BigDecimal(quantity)));
        } else {
            redirectAttributes.addFlashAttribute("success", "Successfully purchased " + quantity + " shares of " + stock.getSymbol());
            logger.info("Stock purchase successful: user={}, stock={}, quantity={}, price=${}", 
                user.getUsername(), stock.getSymbol(), quantity, stock.getCurrentPrice());
        }
        
        return "redirect:/";
    }
    
    @PostMapping("/sell")
    public String sellStock(@RequestParam String stockId,
                           @RequestParam int quantity,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.getUserById(userId);
        Optional<Stock> stockOpt = stockService.getStockById(stockId);
        
        if (userOpt.isEmpty() || stockOpt.isEmpty() || quantity <= 0) {
            redirectAttributes.addFlashAttribute("error", "Invalid transaction");
            logger.warn("Invalid sell transaction: userId={}, stockId={}, quantity={}", userId, stockId, quantity);
            return "redirect:/";
        }
        
        User user = userOpt.get();
        Stock stock = stockOpt.get();
        boolean success = transactionService.sellStock(user, stock, quantity);
        
        if (!success) {
            redirectAttributes.addFlashAttribute("error", "Insufficient stock quantity: You don't own " + quantity + " shares of " + stock.getSymbol());
            logger.warn("Insufficient stock quantity: user={}, stock={}, requested quantity={}", 
                user.getUsername(), stock.getSymbol(), quantity);
        } else {
            redirectAttributes.addFlashAttribute("success", "Successfully sold " + quantity + " shares of " + stock.getSymbol());
            logger.info("Stock sale successful: user={}, stock={}, quantity={}, price=${}", 
                user.getUsername(), stock.getSymbol(), quantity, stock.getCurrentPrice());
        }
        
        return "redirect:/";
    }
} 