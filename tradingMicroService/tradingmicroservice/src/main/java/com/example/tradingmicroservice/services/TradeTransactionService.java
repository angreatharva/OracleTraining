package com.example.tradingmicroservice.services;

import com.example.tradingmicroservice.dto.request.CreateTradeTransactionRequest;
import com.example.tradingmicroservice.dto.response.TradeTransactionResponse;
import com.example.tradingmicroservice.entities.TradeTransaction;
import com.example.tradingmicroservice.enums.TransactionStatus;
import com.example.tradingmicroservice.enums.TransactionType;
import com.example.tradingmicroservice.exceptions.TradeTransactionNotFoundException;
import com.example.tradingmicroservice.repositories.TradeTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional
public class TradeTransactionService implements ITradeTransactionService {

    private final TradeTransactionRepository transactionRepository;

    public TradeTransactionService(TradeTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public TradeTransactionResponse create(CreateTradeTransactionRequest request) {
        LocalDateTime now = LocalDateTime.now();
        TradeTransaction transaction = TradeTransaction.builder()
                .portfolioAccountId(request.portfolioAccountId())
                .holdingId(request.holdingId())
                .productId(request.productId())
                .transactionType(TransactionType.from(request.transactionType()).name())
                .quantity(request.quantity())
                .unitPrice(request.unitPrice())
                .totalAmount(request.quantity().multiply(request.unitPrice()))
                .transactionStatus(TransactionStatus.PENDING.name())
                .transactionDate(request.transactionDate() == null ? now : request.transactionDate())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return toResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional(readOnly = true)
    public TradeTransactionResponse getById(Long id) {
        return toResponse(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeTransactionResponse> getAll(Long portfolioAccountId, String status, String type,
                                                  LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }

        Stream<TradeTransaction> transactions = portfolioAccountId == null
                ? transactionRepository.findAll().stream()
                : transactionRepository.findByPortfolioAccountId(portfolioAccountId).stream();

        return transactions
                .filter(transaction -> status == null || transaction.getTransactionStatus().equalsIgnoreCase(status))
                .filter(transaction -> type == null || transaction.getTransactionType().equalsIgnoreCase(type))
                .filter(transaction -> startDate == null || !transaction.getTransactionDate().isBefore(startDate))
                .filter(transaction -> endDate == null || !transaction.getTransactionDate().isAfter(endDate))
                .map(this::toResponse)
                .toList();
    }

    private TradeTransaction getEntityById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TradeTransactionNotFoundException(id));
    }

    private TradeTransactionResponse toResponse(TradeTransaction transaction) {
        return new TradeTransactionResponse(
                transaction.getTransactionId(),
                transaction.getPortfolioAccountId(),
                transaction.getHoldingId(),
                transaction.getProductId(),
                transaction.getTransactionType(),
                transaction.getQuantity(),
                transaction.getUnitPrice(),
                transaction.getTotalAmount(),
                transaction.getTransactionStatus(),
                transaction.getTransactionDate()
        );
    }
}
