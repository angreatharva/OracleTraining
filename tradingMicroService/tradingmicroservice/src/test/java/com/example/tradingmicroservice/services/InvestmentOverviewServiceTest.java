package com.example.tradingmicroservice.services;

import com.example.tradingmicroservice.clients.PortfolioServiceClient;
import com.example.tradingmicroservice.clients.ProductServiceClient;
import com.example.tradingmicroservice.clients.model.PortfolioAccountSummary;
import com.example.tradingmicroservice.clients.model.PortfolioHoldingSnapshot;
import com.example.tradingmicroservice.clients.model.ProductQuote;
import com.example.tradingmicroservice.dto.response.InvestmentOverviewResponse;
import com.example.tradingmicroservice.dto.response.InvestmentPositionResponse;
import com.example.tradingmicroservice.entities.TradeTransaction;
import com.example.tradingmicroservice.repositories.TradeTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestmentOverviewServiceTest {

    @Mock private TradeTransactionRepository transactionRepository;
    @Mock private PortfolioServiceClient portfolioServiceClient;
    @Mock private ProductServiceClient productServiceClient;
    @InjectMocks private InvestmentOverviewService investmentOverviewService;

    @Test
    void displaysUserProductQuantityPricesAndCurrentValuation() {
        when(portfolioServiceClient.getAccountByUser(1L))
                .thenReturn(new PortfolioAccountSummary(10L, 1L));
        when(portfolioServiceClient.getHoldings(10L)).thenReturn(List.of(
                new PortfolioHoldingSnapshot(25L, 10L, 7L,
                        new BigDecimal("12.00"), new BigDecimal("150.50"), new BigDecimal("1806.00"))));
        when(productServiceClient.getProduct(7L))
                .thenReturn(new ProductQuote(7L, "Demo Stock", new BigDecimal("175.00"), true));
        when(transactionRepository.findByPortfolioAccountId(10L)).thenReturn(List.of(
                trade(101L, "BUY", "10.00", "150.50", "1505.00", LocalDateTime.of(2026, 7, 1, 10, 0)),
                trade(102L, "BUY", "5.00", "160.00", "800.00", LocalDateTime.of(2026, 7, 5, 10, 0)),
                trade(103L, "SELL", "3.00", "175.00", "525.00", LocalDateTime.of(2026, 7, 10, 10, 0))));

        InvestmentOverviewResponse response = investmentOverviewService.getForUser(1L);
        InvestmentPositionResponse position = response.positions().get(0);

        assertEquals("Demo Stock", position.productName());
        assertEquals(new BigDecimal("15.0000"), position.boughtQuantity());
        assertEquals(new BigDecimal("3.0000"), position.soldQuantity());
        assertEquals(new BigDecimal("12.00"), position.currentQuantity());
        assertEquals(new BigDecimal("153.6667"), position.averageBuyPrice());
        assertEquals(new BigDecimal("175.00"), position.currentPrice());
        assertEquals(new BigDecimal("2100.0000"), position.currentValuation());

        System.out.println("\n=== USER INVESTMENT OVERVIEW ===");
        System.out.println(response);
    }

    private TradeTransaction trade(Long id, String type, String quantity, String price,
                                   String total, LocalDateTime date) {
        return TradeTransaction.builder()
                .transactionId(id)
                .portfolioAccountId(10L)
                .productId(7L)
                .transactionType(type)
                .quantity(new BigDecimal(quantity))
                .unitPrice(new BigDecimal(price))
                .totalAmount(new BigDecimal(total))
                .transactionStatus("COMPLETED")
                .transactionDate(date)
                .build();
    }
}
