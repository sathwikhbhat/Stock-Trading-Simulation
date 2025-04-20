package com.stocksim.controller;

import com.stocksim.model.User;
import com.stocksim.service.PortfolioService;
import com.stocksim.service.StockService;
import com.stocksim.service.TransactionService;
import com.stocksim.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class HomeController {
    
    private final UserService userService;
    private final StockService stockService;
    private final PortfolioService portfolioService;
    private final TransactionService transactionService;
    
    @Autowired
    public HomeController(UserService userService, 
                         StockService stockService,
                         PortfolioService portfolioService,
                         TransactionService transactionService) {
        this.userService = userService;
        this.stockService = stockService;
        this.portfolioService = portfolioService;
        this.transactionService = transactionService;
    }
    
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        // Check if user is logged in
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            session.removeAttribute("userId");
            return "redirect:/login";
        }
        
        User user = userOpt.get();
        model.addAttribute("user", user);
        model.addAttribute("stocks", stockService.getAllStocks());
        model.addAttribute("portfolioItems", portfolioService.getPortfolioByUser(user));
        model.addAttribute("transactions", transactionService.getTransactionsByUser(user));
        model.addAttribute("totalValue", portfolioService.calculateTotalPortfolioValue(user));
        model.addAttribute("totalProfitLoss", portfolioService.calculateTotalProfitLoss(user));
        
        return "home";
    }
    
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @PostMapping("/login")
    public String login(@RequestParam String username, 
                      @RequestParam String password,
                      HttpSession session,
                      Model model) {
        if (userService.authenticateUser(username, password)) {
            Optional<User> userOpt = userService.getUserByUsername(username);
            if (userOpt.isPresent()) {
                session.setAttribute("userId", userOpt.get().getId());
                return "redirect:/";
            }
        }
        
        model.addAttribute("error", "Invalid username or password");
        return "login";
    }
    
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
    
    @PostMapping("/register")
    public String register(@RequestParam String username,
                         @RequestParam String password,
                         @RequestParam String email,
                         Model model) {
        // Check if username or email is already taken
        if (userService.getUserByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already taken");
            return "register";
        }
        
        User user = userService.registerUser(username, password, email);
        model.addAttribute("success", "Registration successful! Please login.");
        return "login";
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("userId");
        return "redirect:/login";
    }
} 