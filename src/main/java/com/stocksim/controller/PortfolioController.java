package com.stocksim.controller;

import com.stocksim.model.User;
import com.stocksim.service.PortfolioService;
import com.stocksim.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
@RequestMapping("/portfolio")
public class PortfolioController {
    
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
        model.addAttribute("user", user);
        model.addAttribute("portfolioItems", portfolioService.getPortfolioByUser(user));
        model.addAttribute("totalValue", portfolioService.calculateTotalPortfolioValue(user));
        model.addAttribute("totalProfitLoss", portfolioService.calculateTotalProfitLoss(user));
        
        return "performance";
    }
} 