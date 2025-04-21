package com.stocksim.service;

import com.stocksim.model.User;
import com.stocksim.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    public User registerUser(String username, String password, String email) {
        // In a real application, you would validate the input and hash the password
        User user = new User(username, password, email, new BigDecimal("100000.00")); // Start with $100,000
        return userRepository.save(user);
    }
    
    public boolean authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        // In a real application, you would compare password hashes, not plaintext
        return userOpt.isPresent() && userOpt.get().getPassword().equals(password);
    }
    
    public void updateAccountBalance(User user, BigDecimal amount) {
        user.setAccountBalance(user.getAccountBalance().add(amount));
        userRepository.save(user);
    }
} 