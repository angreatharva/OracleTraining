package com.example.tradingmicroservice.services;

import com.example.tradingmicroservice.dto.request.CreateTradeTransactionRequest;
import com.example.tradingmicroservice.dto.response.TradeTransactionResponse;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeTransactionServiceTest {

    @Mock
    private TradeTransactionRepository transactionRepository;

    @InjectMocks
    private TradeTransactionService transactionService;

    @Test
    void createCalculatesAmountAndSetsPendingStatus() {
        when(transactionRepository.save(any(TradeTransaction.class))).thenAnswer(invocation -> {
            TradeTransaction transaction = invocation.getArgument(0);
            transaction.setTransactionId(101L);
            return transaction;
        });

        TradeTransactionResponse response = transactionService.create(new CreateTradeTransactionRequest(
                1L, 2L, 3L, "buy", new BigDecimal("2.00"), new BigDecimal("150.50"), null
        ));

        ArgumentCaptor<TradeTransaction> captor = ArgumentCaptor.forClass(TradeTransaction.class);
        verify(transactionRepository).save(captor.capture());

        assertEquals(new BigDecimal("301.0000"), captor.getValue().getTotalAmount());
        assertEquals("BUY", response.transactionType());
        assertEquals("PENDING", response.transactionStatus());
        assertEquals(101L, response.transactionId());
    }

    @Test
    void createRejectsUnknownTransactionType() {
        CreateTradeTransactionRequest request = new CreateTradeTransactionRequest(
                1L, 2L, 3L, "TRANSFER", BigDecimal.ONE, BigDecimal.ONE, null
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.create(request)
        );

        assertEquals("transactionType must be BUY or SELL", exception.getMessage());
    }
}
