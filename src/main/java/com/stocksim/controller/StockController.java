package com.stocksim.controller;

import com.stocksim.model.Stock;
import com.stocksim.model.User;
import com.stocksim.service.StockService;
import com.stocksim.service.TransactionService;
import com.stocksim.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
@RequestMapping("/stocks")
public class StockController {
    
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
                          Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.getUserById(userId);
        Optional<Stock> stockOpt = stockService.getStockById(stockId);
        
        if (userOpt.isEmpty() || stockOpt.isEmpty() || quantity <= 0) {
            model.addAttribute("error", "Invalid transaction");
            return "redirect:/";
        }
        
        boolean success = transactionService.buyStock(userOpt.get(), stockOpt.get(), quantity);
        
        if (!success) {
            model.addAttribute("error", "Insufficient funds");
        } else {
            model.addAttribute("success", "Stock purchased successfully");
        }
        
        return "redirect:/";
    }
    
    @PostMapping("/sell")
    public String sellStock(@RequestParam String stockId,
                           @RequestParam int quantity,
                           HttpSession session,
                           Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.getUserById(userId);
        Optional<Stock> stockOpt = stockService.getStockById(stockId);
        
        if (userOpt.isEmpty() || stockOpt.isEmpty() || quantity <= 0) {
            model.addAttribute("error", "Invalid transaction");
            return "redirect:/";
        }
        
        boolean success = transactionService.sellStock(userOpt.get(), stockOpt.get(), quantity);
        
        if (!success) {
            model.addAttribute("error", "Insufficient stock quantity");
        } else {
            model.addAttribute("success", "Stock sold successfully");
        }
        
        return "redirect:/";
    }
    
    // For demonstration purposes, trigger the stock price update
    @GetMapping("/update-prices")
    @ResponseBody
    public String updatePrices() {
        stockService.updateStockPrices();
        return "Stock prices updated";
    }
} 