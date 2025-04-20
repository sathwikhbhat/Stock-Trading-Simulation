package com.stocksim.controller;

import com.stocksim.model.Portfolio;
import com.stocksim.model.User;
import com.stocksim.service.PortfolioService;
import com.stocksim.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/portfolio")
public class PortfolioController {
    
    private static final Logger logger = LoggerFactory.getLogger(PortfolioController.class);
    private final PortfolioService portfolioService;
    private final UserService userService;
    
    @Autowired
    public PortfolioController(PortfolioService portfolioService, UserService userService) {
        this.portfolioService = portfolioService;
        this.userService = userService;
    }
    
    @GetMapping
    public String getPortfolio(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        User user = userOpt.get();
        model.addAttribute("user", user);
        model.addAttribute("portfolioItems", portfolioService.getPortfolioByUser(user));
        model.addAttribute("totalValue", portfolioService.calculateTotalPortfolioValue(user));
        model.addAttribute("totalProfitLoss", portfolioService.calculateTotalProfitLoss(user));
        
        return "portfolio";
    }
    
    @GetMapping("/performance")
    public String getPerformance(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        User user = userOpt.get();
        List<Portfolio> portfolioItems = portfolioService.getPortfolioByUser(user);
        BigDecimal totalValue = portfolioService.calculateTotalPortfolioValue(user);
        BigDecimal totalProfitLoss = portfolioService.calculateTotalProfitLoss(user);
        
        // Prepare data for charts
        List<String> symbols = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        List<Double> profitLossValues = new ArrayList<>();
        
        for (Portfolio item : portfolioItems) {
            symbols.add(item.getStock().getSymbol());
            values.add(item.getCurrentValue().doubleValue());
            profitLossValues.add(item.getProfitLoss().doubleValue());
        }
        
        // Add to model
        model.addAttribute("user", user);
        model.addAttribute("portfolioItems", portfolioItems);
        model.addAttribute("totalValue", totalValue);
        model.addAttribute("totalProfitLoss", totalProfitLoss);
        
        // Chart data
        model.addAttribute("chartSymbols", symbols);
        model.addAttribute("chartValues", values);
        model.addAttribute("chartProfitLoss", profitLossValues);
        
        logger.info("Prepared chart data for {} portfolio items", portfolioItems.size());
        
        return "performance";
    }
} 