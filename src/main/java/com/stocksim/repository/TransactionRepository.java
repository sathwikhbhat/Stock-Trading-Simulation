package com.stocksim.repository;

import com.stocksim.model.Transaction;
import com.stocksim.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findByUser(User user);
    List<Transaction> findByUserOrderByTimestampDesc(User user);
} 