package com.example.tradingmicroservice.services;

import com.example.tradingmicroservice.dto.request.CreateTradeTransactionRequest;
import com.example.tradingmicroservice.dto.response.TradeTransactionResponse;
import com.example.tradingmicroservice.clients.BankServiceClient;
import com.example.tradingmicroservice.clients.PortfolioServiceClient;
import com.example.tradingmicroservice.clients.ProductServiceClient;
import com.example.tradingmicroservice.clients.model.DebitResult;
import com.example.tradingmicroservice.clients.model.ProductQuote;
import com.example.tradingmicroservice.entities.TradeTransaction;
import com.example.tradingmicroservice.repositories.TradeTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeTransactionServiceTest {

    @Mock
    private TradeTransactionRepository transactionRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private BankServiceClient bankServiceClient;

    @Mock
    private PortfolioServiceClient portfolioServiceClient;

    @InjectMocks
    private TradeTransactionService transactionService;

    @Test
    void buyUsesProductPriceAndCompletesAfterBankAndPortfolioSucceed() {
        when(transactionRepository.save(any(TradeTransaction.class))).thenAnswer(invocation -> {
            TradeTransaction transaction = invocation.getArgument(0);
            transaction.setTransactionId(101L);
            return transaction;
        });
        when(productServiceClient.getActiveProduct(3L))
                .thenReturn(new ProductQuote(3L, "Fund", new BigDecimal("151.25"), true));
        when(bankServiceClient.authorizeDebit(org.mockito.ArgumentMatchers.eq(4L), any()))
                .thenReturn(new DebitResult(true, "TRADE-101", null));

        TradeTransactionResponse response = transactionService.create(new CreateTradeTransactionRequest(
                1L, 2L, 3L, 4L, "buy", new BigDecimal("2.00"), new BigDecimal("150.50")
        ));

        ArgumentCaptor<TradeTransaction> captor = ArgumentCaptor.forClass(TradeTransaction.class);
        verify(transactionRepository, atLeastOnce()).save(captor.capture());

        TradeTransaction persisted = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals(new BigDecimal("302.5000"), persisted.getTotalAmount());
        assertEquals("BUY", response.transactionType());
        assertEquals("COMPLETED", response.transactionStatus());
        assertEquals(101L, response.transactionId());
        verify(portfolioServiceClient).applyCompletedTrade(any());
    }

    @Test
    void createRejectsUnknownTransactionType() {
        CreateTradeTransactionRequest request = new CreateTradeTransactionRequest(
                1L, 2L, 3L, 4L, "TRANSFER", BigDecimal.ONE, BigDecimal.ONE
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.create(request)
        );

        assertEquals("transactionType must be BUY or SELL", exception.getMessage());
    }

    @Test
    void buyIsMarkedFailedWhenBankDeclinesDebit() {
        when(transactionRepository.save(any(TradeTransaction.class))).thenAnswer(invocation -> {
            TradeTransaction transaction = invocation.getArgument(0);
            transaction.setTransactionId(102L);
            return transaction;
        });
        when(productServiceClient.getActiveProduct(3L))
                .thenReturn(new ProductQuote(3L, "Fund", new BigDecimal("150.00"), true));
        when(bankServiceClient.authorizeDebit(org.mockito.ArgumentMatchers.eq(4L), any()))
                .thenReturn(new DebitResult(false, "TRADE-102", "Insufficient balance"));

        TradeTransactionResponse response = transactionService.create(new CreateTradeTransactionRequest(
                1L, 2L, 3L, 4L, "BUY", BigDecimal.ONE, new BigDecimal("150.00")));

        assertEquals("FAILED", response.transactionStatus());
        assertEquals("Insufficient balance", response.failureReason());
        verify(portfolioServiceClient).validateTrade(any());
    }
}
