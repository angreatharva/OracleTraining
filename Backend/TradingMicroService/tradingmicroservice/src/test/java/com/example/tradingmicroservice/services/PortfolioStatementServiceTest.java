package com.example.tradingmicroservice.services;

import com.example.tradingmicroservice.dto.request.CreatePortfolioStatementRequest;
import com.example.tradingmicroservice.dto.response.PortfolioStatementResponse;
import com.example.tradingmicroservice.entities.PortfolioStatement;
import com.example.tradingmicroservice.entities.TradeTransaction;
import com.example.tradingmicroservice.repositories.PortfolioStatementRepository;
import com.example.tradingmicroservice.repositories.TradeTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioStatementServiceTest {

    @Mock
    private PortfolioStatementRepository statementRepository;

    @Mock
    private TradeTransactionRepository transactionRepository;

    @InjectMocks
    private PortfolioStatementService statementService;

    @Test
    void createLinksThePrimaryTransaction() {
        TradeTransaction transaction = TradeTransaction.builder().transactionId(10L).build();
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(transaction));
        when(statementRepository.save(any(PortfolioStatement.class))).thenAnswer(invocation -> {
            PortfolioStatement statement = invocation.getArgument(0);
            statement.setStatementId(20L);
            return statement;
        });

        PortfolioStatementResponse response = statementService.create(new CreatePortfolioStatementRequest(
                1L, 2L, 10L,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31),
                new BigDecimal("1000.00"), new BigDecimal("1200.00"), null
        ));

        assertEquals(20L, response.statementId());
        assertEquals(10L, response.transactionId());
        assertEquals(1, response.transactionIds().size());
        assertEquals(10L, response.transactionIds().get(0));
    }

    @Test
    void createRejectsAnInvalidStatementDateRange() {
        CreatePortfolioStatementRequest request = new CreatePortfolioStatementRequest(
                1L, 2L, 10L,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 7, 31),
                BigDecimal.ZERO, BigDecimal.ONE, null
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> statementService.create(request)
        );

        assertEquals("statementStart must be before or equal to statementEnd", exception.getMessage());
    }
}
