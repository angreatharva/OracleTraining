package com.example.tradingmicroservice.simulation;

import com.example.tradingmicroservice.clients.BankServiceClient;
import com.example.tradingmicroservice.clients.PortfolioServiceClient;
import com.example.tradingmicroservice.clients.ProductServiceClient;
import com.example.tradingmicroservice.clients.model.CreditRequest;
import com.example.tradingmicroservice.clients.model.CreditResult;
import com.example.tradingmicroservice.clients.model.DebitRequest;
import com.example.tradingmicroservice.clients.model.DebitResult;
import com.example.tradingmicroservice.clients.model.HoldingUpdateRequest;
import com.example.tradingmicroservice.clients.model.PortfolioAccountSummary;
import com.example.tradingmicroservice.clients.model.PortfolioHoldingSnapshot;
import com.example.tradingmicroservice.clients.model.ProductQuote;
import com.example.tradingmicroservice.dto.request.CreatePortfolioStatementRequest;
import com.example.tradingmicroservice.dto.request.CreateTradeTransactionRequest;
import com.example.tradingmicroservice.dto.response.PortfolioStatementResponse;
import com.example.tradingmicroservice.dto.response.TradeTransactionResponse;
import com.example.tradingmicroservice.entities.PortfolioStatement;
import com.example.tradingmicroservice.entities.TradeTransaction;
import com.example.tradingmicroservice.repositories.PortfolioStatementRepository;
import com.example.tradingmicroservice.repositories.TradeTransactionRepository;
import com.example.tradingmicroservice.services.PortfolioStatementService;
import com.example.tradingmicroservice.services.TradeTransactionService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** A readable business-flow simulation; it deliberately uses no assertions. */
class TradingBuySellSimulationTest {

    private static final Logger log = LoggerFactory.getLogger(TradingBuySellSimulationTest.class);

    @Test
    void simulateBuyingThenSellingStockAndPrintEveryServiceCall() {
        log.info("========== STOCK BUY / SELL SIMULATION START ==========");

        Map<Long, TradeTransaction> transactionStore = new LinkedHashMap<>();
        AtomicLong transactionIds = new AtomicLong(1);
        TradeTransactionRepository transactionRepository = mock(TradeTransactionRepository.class);
        when(transactionRepository.save(any(TradeTransaction.class))).thenAnswer(invocation -> {
            TradeTransaction transaction = invocation.getArgument(0);
            if (transaction.getTransactionId() == null) {
                transaction.setTransactionId(transactionIds.getAndIncrement());
            }
            transactionStore.put(transaction.getTransactionId(), transaction);
            log.info("[TRADING-SERVICE] Saved trade id={}, type={}, status={}, total={}",
                    transaction.getTransactionId(), transaction.getTransactionType(),
                    transaction.getTransactionStatus(), transaction.getTotalAmount());
            return transaction;
        });
        when(transactionRepository.findById(anyLong()))
                .thenAnswer(invocation -> Optional.ofNullable(transactionStore.get(invocation.getArgument(0))));

        SimulatedProductService product = new SimulatedProductService();
        SimulatedBankService bank = new SimulatedBankService(new BigDecimal("1000.00"));
        SimulatedPortfolioService portfolio = new SimulatedPortfolioService(new BigDecimal("10.00"));
        TradeTransactionService trading = new TradeTransactionService(
                transactionRepository, product, bank, portfolio);

        log.info("Initial state: cash={}, DEMO-STOCK quantity={}", bank.balance, portfolio.quantity);

        log.info("--- BUY 2 DEMO-STOCK shares ---");
        TradeTransactionResponse buy = trading.create(new CreateTradeTransactionRequest(
                1L, 1L, 1L, 11L, "BUY", new BigDecimal("2.00"), BigDecimal.ONE));
        log.info("BUY result: status={}, executed price={}, total={}",
                buy.transactionStatus(), buy.unitPrice(), buy.totalAmount());
        log.info("After BUY: cash={}, quantity={}", bank.balance, portfolio.quantity);

        product.currentPrice = new BigDecimal("175.00");
        log.info("[PRODUCT-SERVICE] Market price changed to {} before SELL", product.currentPrice);

        log.info("--- SELL 1 DEMO-STOCK share ---");
        TradeTransactionResponse sell = trading.create(new CreateTradeTransactionRequest(
                1L, 1L, 1L, 11L, "SELL", BigDecimal.ONE, BigDecimal.ONE));
        log.info("SELL result: status={}, executed price={}, total={}",
                sell.transactionStatus(), sell.unitPrice(), sell.totalAmount());
        log.info("After SELL: cash={}, quantity={}", bank.balance, portfolio.quantity);

        PortfolioStatementResponse statement = createStatement(transactionRepository, transactionStore, buy, sell, portfolio);
        log.info("[PORTFOLIO-STATEMENT] period={} to {}, openingValue={}, closingValue={}, tradeIds={}",
                statement.statementStart(), statement.statementEnd(), statement.openingValue(),
                statement.closingValue(), statement.transactionIds());
        log.info("========== SIMULATION COMPLETE ==========");
    }

    private PortfolioStatementResponse createStatement(TradeTransactionRepository transactionRepository,
                                                        Map<Long, TradeTransaction> transactionStore,
                                                        TradeTransactionResponse buy,
                                                        TradeTransactionResponse sell,
                                                        SimulatedPortfolioService portfolio) {
        PortfolioStatementRepository statementRepository = mock(PortfolioStatementRepository.class);
        AtomicLong statementIds = new AtomicLong(1);
        when(statementRepository.save(any(PortfolioStatement.class))).thenAnswer(invocation -> {
            PortfolioStatement statement = invocation.getArgument(0);
            statement.setStatementId(statementIds.getAndIncrement());
            return statement;
        });

        PortfolioStatementService statementService = new PortfolioStatementService(statementRepository, transactionRepository);
        BigDecimal openingValue = new BigDecimal("10.00").multiply(new BigDecimal("150.50"));
        BigDecimal closingValue = portfolio.quantity.multiply(new BigDecimal("175.00"));
        return statementService.create(new CreatePortfolioStatementRequest(
                1L, 1L, buy.transactionId(), LocalDate.now().minusDays(1), LocalDate.now(),
                openingValue, closingValue, List.of(sell.transactionId())));
    }

    private static final class SimulatedProductService implements ProductServiceClient {
        private BigDecimal currentPrice = new BigDecimal("150.50");

        @Override
        public ProductQuote getProduct(Long productId) {
            log.info("[PRODUCT-SERVICE] Product {} found: DEMO-STOCK, currentPrice={}", productId, currentPrice);
            return new ProductQuote(productId, "DEMO-STOCK", currentPrice, true);
        }
    }

    private static final class SimulatedBankService implements BankServiceClient {
        private BigDecimal balance;

        private SimulatedBankService(BigDecimal openingBalance) {
            this.balance = openingBalance;
        }

        @Override
        public DebitResult authorizeDebit(Long bankAccountId, DebitRequest request) {
            log.info("[BANK-SERVICE] Debit check: account={}, balance={}, requested={}",
                    bankAccountId, balance, request.amount());
            if (balance.compareTo(request.amount()) < 0) {
                log.info("[BANK-SERVICE] Debit rejected: insufficient balance");
                return new DebitResult(false, request.transactionReference(), "Insufficient balance");
            }
            balance = balance.subtract(request.amount());
            log.info("[BANK-SERVICE] Debit approved. New balance={}", balance);
            return new DebitResult(true, request.transactionReference(), null);
        }

        @Override
        public CreditResult credit(Long bankAccountId, CreditRequest request) {
            balance = balance.add(request.amount());
            log.info("[BANK-SERVICE] Credit approved: account={}, amount={}, new balance={}",
                    bankAccountId, request.amount(), balance);
            return new CreditResult(true, request.transactionReference(), null);
        }
    }

    private static final class SimulatedPortfolioService implements PortfolioServiceClient {
        private BigDecimal quantity;

        private SimulatedPortfolioService(BigDecimal openingQuantity) {
            this.quantity = openingQuantity;
        }

        @Override
        public void validateTrade(HoldingUpdateRequest request) {
            if ("SELL".equals(request.transactionType()) && quantity.compareTo(request.quantity()) < 0) {
                throw new IllegalArgumentException("Insufficient holding quantity");
            }
            if (!"BUY".equals(request.transactionType()) && !"SELL".equals(request.transactionType())) {
                throw new IllegalArgumentException("transactionType must be BUY or SELL");
            }
            log.info("[PORTFOLIO-SERVICE] Trade validation passed for {}", request.transactionType());
        }

        @Override
        public void applyCompletedTrade(HoldingUpdateRequest request) {
            log.info("[PORTFOLIO-SERVICE] Applying {} of {} shares to holding {}",
                    request.transactionType(), request.quantity(), request.holdingId());
            if ("BUY".equals(request.transactionType())) {
                quantity = quantity.add(request.quantity());
            } else if (quantity.compareTo(request.quantity()) >= 0) {
                quantity = quantity.subtract(request.quantity());
            } else {
                throw new IllegalArgumentException("Insufficient holding quantity");
            }
            log.info("[PORTFOLIO-SERVICE] Holding quantity is now {}", quantity);
        }

        @Override public PortfolioAccountSummary getAccountByUser(Long userId) { return new PortfolioAccountSummary(1L, userId); }
        @Override public List<PortfolioHoldingSnapshot> getHoldings(Long portfolioAccountId) { return new ArrayList<>(); }
    }
}
