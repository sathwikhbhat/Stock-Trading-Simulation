package com.stocksim.controller;

import com.stocksim.model.Stock;
import com.stocksim.model.User;
import com.stocksim.service.StockService;
import com.stocksim.service.TransactionService;
import com.stocksim.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        Optional<Stock> stockOpt = stockService.getStockById(id);
        if (stockOpt.isEmpty()) {
            return "redirect:/stocks";
        }
        
        model.addAttribute("user", userOpt.get());
        model.addAttribute("stock", stockOpt.get());
        return "stock-details";
    }
    
    @GetMapping("/{id}/history")
    @ResponseBody
    public ResponseEntity<?> getStockPriceHistory(@PathVariable String id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        
        Optional<Stock> stockOpt = stockService.getStockById(id);
        if (stockOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Stock stock = stockOpt.get();
        
        // Check if price history exists
        if (stock.getPriceHistory() == null || stock.getPriceHistory().isEmpty()) {
            logger.warn("No price history found for stock: {}", stock.getSymbol());
            // Return empty data structure
            Map<String, Object> emptyResponse = new HashMap<>();
            emptyResponse.put("symbol", stock.getSymbol());
            emptyResponse.put("name", stock.getName());
            emptyResponse.put("data", new ArrayList<>());
            return ResponseEntity.ok(emptyResponse);
        }
        
        // Sort price history by timestamp (oldest to newest)
        List<Stock.PricePoint> sortedHistory = new ArrayList<>(stock.getPriceHistory());
        sortedHistory.sort((p1, p2) -> Long.compare(p1.getTimestamp(), p2.getTimestamp()));
        
        // Convert price history to a format suitable for Chart.js
        List<Map<String, Object>> chartData = sortedHistory.stream()
            .map(point -> {
                Map<String, Object> data = new HashMap<>();
                data.put("x", point.getTimestamp());
                data.put("y", point.getPrice());
                return data;
            })
            .collect(Collectors.toList());
        
        if (!chartData.isEmpty()) {
            logger.info("Returning price history for {}: {} points with timestamps from {} to {}", 
                stock.getSymbol(), 
                chartData.size(),
                new java.util.Date((Long)chartData.get(0).get("x")),
                new java.util.Date((Long)chartData.get(chartData.size()-1).get("x"))
            );
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("symbol", stock.getSymbol());
        response.put("name", stock.getName());
        response.put("data", chartData);
        
        return ResponseEntity.ok(response);
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