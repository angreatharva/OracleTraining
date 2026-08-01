package com.example.tradingmicroservice.services;

import com.example.tradingmicroservice.clients.BankServiceClient;
import com.example.tradingmicroservice.clients.PortfolioServiceClient;
import com.example.tradingmicroservice.clients.ProductServiceClient;
import com.example.tradingmicroservice.clients.model.CreditRequest;
import com.example.tradingmicroservice.clients.model.CreditResult;
import com.example.tradingmicroservice.clients.model.DebitRequest;
import com.example.tradingmicroservice.clients.model.DebitResult;
import com.example.tradingmicroservice.clients.model.HoldingUpdateRequest;
import com.example.tradingmicroservice.clients.model.ProductQuote;
import com.example.tradingmicroservice.dto.request.CreateTradeTransactionRequest;
import com.example.tradingmicroservice.dto.response.TradeTransactionResponse;
import com.example.tradingmicroservice.entities.TradeTransaction;
import com.example.tradingmicroservice.enums.TransactionStatus;
import com.example.tradingmicroservice.enums.TransactionType;
import com.example.tradingmicroservice.exceptions.TradeTransactionNotFoundException;
import com.example.tradingmicroservice.repositories.TradeTransactionRepository;
import com.example.tradingmicroservice.services.ITradeTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Service
public class TradeTransactionService implements ITradeTransactionService {

    private static final Logger log = LoggerFactory.getLogger(TradeTransactionService.class);

    private final TradeTransactionRepository transactionRepository;
    private final ProductServiceClient productServiceClient;
    private final BankServiceClient bankServiceClient;
    private final PortfolioServiceClient portfolioServiceClient;

    public TradeTransactionService(TradeTransactionRepository transactionRepository,
                                   ProductServiceClient productServiceClient,
                                   BankServiceClient bankServiceClient,
                                   PortfolioServiceClient portfolioServiceClient) {
        this.transactionRepository = transactionRepository;
        this.productServiceClient = productServiceClient;
        this.bankServiceClient = bankServiceClient;
        this.portfolioServiceClient = portfolioServiceClient;
    }

    @Override
    public TradeTransactionResponse create(CreateTradeTransactionRequest request) {
        TransactionType transactionType = TransactionType.from(request.transactionType());
        LocalDateTime now = LocalDateTime.now();
        TradeTransaction transaction = TradeTransaction.builder()
                .portfolioAccountId(request.portfolioAccountId())
                .holdingId(request.holdingId())
                .productId(request.productId())
                .transactionType(transactionType.name())
                .quantity(request.quantity())
                .unitPrice(request.unitPrice())
                .totalAmount(request.quantity().multiply(request.unitPrice()))
                .transactionStatus(TransactionStatus.PENDING.name())
                .transactionDate(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Commit the local PENDING record before making any remote call. Keeping this
        // transaction open can lock referenced Bank/Portfolio rows in a shared schema
        // and deadlock the service-to-service workflow.
        transaction = transactionRepository.save(transaction);
        try {
            log.info("[TRADE-{}] Calling Product Service for product {}", transaction.getTransactionId(), request.productId());
            ProductQuote product = productServiceClient.getActiveProduct(request.productId());
            applyExecutionPrice(transaction, product.currentPrice());
            transactionRepository.save(transaction);
            log.info("[TRADE-{}] Product Service returned execution price {}", transaction.getTransactionId(), transaction.getUnitPrice());

            String reference = "TRADE-" + transaction.getTransactionId();
            HoldingUpdateRequest holdingUpdate = new HoldingUpdateRequest(
                    request.portfolioAccountId(), request.holdingId(), request.productId(),
                    transactionType.name(), request.quantity(), transaction.getUnitPrice(), transaction.getTransactionId());

            log.info("[TRADE-{}] Validating Portfolio trade before moving money", transaction.getTransactionId());
            portfolioServiceClient.validateTrade(holdingUpdate);
            log.info("[TRADE-{}] Portfolio trade validation completed", transaction.getTransactionId());

            if (transactionType == TransactionType.BUY) {
                return executeBuy(transaction, request.bankAccountId(), reference, holdingUpdate);
            }
            return executeSell(transaction, request.bankAccountId(), reference, holdingUpdate);
        } catch (RuntimeException exception) {
            log.error("[TRADE-{}] Trade failed before completion: {}", transaction.getTransactionId(), safeMessage(exception), exception);
            return fail(transaction, "Trade execution failed: " + safeMessage(exception));
        }
    }

    private TradeTransactionResponse executeBuy(TradeTransaction transaction, Long bankAccountId,
                                                String reference, HoldingUpdateRequest holdingUpdate) {
        log.info("[TRADE-{}] Calling Bank Service to debit account {} for {}", transaction.getTransactionId(), bankAccountId, transaction.getTotalAmount());
        DebitResult debit = bankServiceClient.authorizeDebit(bankAccountId,
                new DebitRequest(transaction.getTotalAmount(), reference));
        if (debit == null || !debit.approved()) {
            log.warn("[TRADE-{}] Bank debit was rejected: {}", transaction.getTransactionId(), debit == null ? "no response" : debit.failureReason());
            return fail(transaction, debit == null ? "Bank service returned no debit result" : debit.failureReason());
        }

        try {
            log.info("[TRADE-{}] Bank debit completed; calling Portfolio Service to apply BUY", transaction.getTransactionId());
            portfolioServiceClient.applyCompletedTrade(holdingUpdate);
            log.info("[TRADE-{}] Portfolio holding updated; marking trade COMPLETED", transaction.getTransactionId());
            return complete(transaction);
        } catch (RuntimeException exception) {
            log.error("[TRADE-{}] Portfolio update failed; attempting bank-credit compensation: {}", transaction.getTransactionId(), safeMessage(exception), exception);
            CreditResult compensation = bankServiceClient.credit(bankAccountId,
                    new CreditRequest(transaction.getTotalAmount(), "REVERSAL-" + reference));
            String outcome = compensation != null && compensation.successful()
                    ? "Bank debit was reversed after portfolio update failed"
                    : "Portfolio update failed and bank debit compensation also failed";
            return fail(transaction, outcome + ": " + safeMessage(exception));
        }
    }

    private TradeTransactionResponse executeSell(TradeTransaction transaction, Long bankAccountId,
                                                 String reference, HoldingUpdateRequest holdingUpdate) {
        try {
            log.info("[TRADE-{}] Calling Portfolio Service to apply SELL", transaction.getTransactionId());
            portfolioServiceClient.applyCompletedTrade(holdingUpdate);
            log.info("[TRADE-{}] Portfolio holding updated; calling Bank Service to credit account {}", transaction.getTransactionId(), bankAccountId);
        } catch (RuntimeException exception) {
            log.error("[TRADE-{}] Portfolio SELL update failed: {}", transaction.getTransactionId(), safeMessage(exception), exception);
            return fail(transaction, "Portfolio update failed: " + safeMessage(exception));
        }

        CreditResult credit = bankServiceClient.credit(bankAccountId,
                new CreditRequest(transaction.getTotalAmount(), reference));
        if (credit != null && credit.successful()) {
            log.info("[TRADE-{}] Bank credit completed; marking trade COMPLETED", transaction.getTransactionId());
            return complete(transaction);
        }

        try {
            portfolioServiceClient.applyCompletedTrade(reverse(holdingUpdate));
            return fail(transaction, credit == null ? "Bank service returned no credit result" : credit.failureReason());
        } catch (RuntimeException compensationFailure) {
            log.error("[TRADE-{}] Bank credit and portfolio reversal both failed: {}", transaction.getTransactionId(), safeMessage(compensationFailure), compensationFailure);
            return fail(transaction, "Bank credit failed and portfolio reversal failed: "
                    + safeMessage(compensationFailure));
        }
    }

    private HoldingUpdateRequest reverse(HoldingUpdateRequest original) {
        return new HoldingUpdateRequest(
                original.portfolioAccountId(), original.holdingId(), original.productId(),
                "SELL".equals(original.transactionType()) ? "BUY" : "SELL",
                original.quantity(), original.unitPrice(), original.transactionId());
    }

    private void applyExecutionPrice(TradeTransaction transaction, BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Product has no valid current price");
        }
        transaction.setUnitPrice(price);
        transaction.setTotalAmount(transaction.getQuantity().multiply(price));
        transaction.setUpdatedAt(LocalDateTime.now());
    }

    private TradeTransactionResponse complete(TradeTransaction transaction) {
        transaction.setTransactionStatus(TransactionStatus.COMPLETED.name());
        transaction.setUpdatedAt(LocalDateTime.now());
        return toResponse(transactionRepository.save(transaction));
    }

    private TradeTransactionResponse fail(TradeTransaction transaction, String reason) {
        transaction.setTransactionStatus(TransactionStatus.FAILED.name());
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
        return toResponse(transaction, reason);
    }

    private String safeMessage(RuntimeException exception) {
        return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
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
        return toResponse(transaction, null);
    }

    private TradeTransactionResponse toResponse(TradeTransaction transaction, String failureReason) {
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
                transaction.getTransactionDate(),
                failureReason
        );
    }
}
