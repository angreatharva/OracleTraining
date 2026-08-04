package com.example.wtms.Services;

import com.example.wtms.Entities.TradeTransaction;
import com.example.wtms.Exceptions.TradeTransactionNotFoundException;
import com.example.wtms.Repositories.TradeTransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TradeTransactionService implements ITradeTransactionService {

    private final TradeTransactionRepository transactionRepository;

    public TradeTransactionService(TradeTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public TradeTransaction create(TradeTransaction transaction) {
        transaction.setTransactionId(null);
        LocalDateTime now = LocalDateTime.now();
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);
        calculateTotalAmount(transaction);
        return transactionRepository.save(transaction);
    }

    @Override
    public TradeTransaction getById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TradeTransactionNotFoundException(id));
    }

    @Override
    public List<TradeTransaction> getAll() {
        return transactionRepository.findAll();
    }

    @Override
    public List<TradeTransaction> getByStatus(String status) {
        return transactionRepository.findByTransactionStatus(status);
    }

    @Override
    public List<TradeTransaction> getByType(String type) {
        return transactionRepository.findByTransactionType(type);
    }

    @Override
    public List<TradeTransaction> getByPortfolioAccountId(Long portfolioAccountId) {
        return transactionRepository.findByPortfolioAccount_PortfolioAccountId(portfolioAccountId);
    }

    @Override
    public List<TradeTransaction> getByHoldingId(Long holdingId) {
        return transactionRepository.findByHolding_HoldingId(holdingId);
    }

    @Override
    public List<TradeTransaction> getByProductId(Long productId) {
        return transactionRepository.findByProduct_ProductId(productId);
    }

    @Override
    public TradeTransaction update(Long id, TradeTransaction transaction) {
        TradeTransaction existing = getById(id);
        transaction.setTransactionId(existing.getTransactionId());
        transaction.setCreatedAt(existing.getCreatedAt());
        transaction.setUpdatedAt(LocalDateTime.now());
        calculateTotalAmount(transaction);
        return transactionRepository.save(transaction);
    }

    @Override
    public void delete(Long id) {
        transactionRepository.delete(getById(id));
    }

    private void calculateTotalAmount(TradeTransaction transaction) {
        if (transaction.getQuantity() != null && transaction.getUnitPrice() != null) {
            transaction.setTotalAmount(transaction.getQuantity().multiply(transaction.getUnitPrice()));
        }
    }
}
