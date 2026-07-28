package com.example.tradingmicroservice.integration;

import com.example.tradingmicroservice.clients.RestBankServiceClient;
import com.example.tradingmicroservice.clients.RestPortfolioServiceClient;
import com.example.tradingmicroservice.clients.RestProductServiceClient;
import com.example.tradingmicroservice.dto.request.CreateTradeTransactionRequest;
import com.example.tradingmicroservice.dto.response.TradeTransactionResponse;
import com.example.tradingmicroservice.entities.TradeTransaction;
import com.example.tradingmicroservice.repositories.TradeTransactionRepository;
import com.example.tradingmicroservice.services.TradeTransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

/**
 * Contract-level test. It executes Trading's real RestClient implementations and
 * verifies the HTTP contracts used for Product, Bank and Portfolio.
 */
class TradingCrossServiceHttpTest {

    private static final String PRODUCT_URL = "http://product-test";
    private static final String BANK_URL = "http://bank-test";
    private static final String PORTFOLIO_URL = "http://portfolio-test";

    @Test
    void buyValidatesPortfolioThenCallsBankAndPortfolioOverHttpAndCompletesTrade() {
        TradeTransactionRepository repository = mock(TradeTransactionRepository.class);
        when(repository.save(any(TradeTransaction.class))).thenAnswer(invocation -> {
            TradeTransaction transaction = invocation.getArgument(0);
            transaction.setTransactionId(700L);
            return transaction;
        });

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), requestTo(PRODUCT_URL + "/api/products/1"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {"productId":1,"productName":"Demo Stock","currentPrice":150.50,"active":true}
                        """, MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo(PORTFOLIO_URL + "/api/portfolios/internal/trades/validate"))
                .andExpect(method(POST))
                .andExpect(content().json("""
                        {"portfolioAccountId":1,"holdingId":9,"productId":1,"transactionType":"BUY",
                        "quantity":2.00,"unitPrice":150.50,"transactionId":700}
                        """))
                .andRespond(withSuccess());
        server.expect(once(), requestTo(BANK_URL + "/api/bank-accounts/11/debit"))
                .andExpect(method(POST))
                .andExpect(content().json("{" + "\"amount\":301.00,\"transactionReference\":\"TRADE-700\"}"))
                .andRespond(withSuccess("""
                        {"approved":true,"reference":"TRADE-700","failureReason":null}
                        """, MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo(PORTFOLIO_URL + "/api/portfolios/internal/trades"))
                .andExpect(method(POST))
                .andExpect(content().json("""
                        {"portfolioAccountId":1,"holdingId":9,"productId":1,"transactionType":"BUY",
                        "quantity":2.00,"unitPrice":150.50,"transactionId":700}
                        """))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        TradeTransactionService service = new TradeTransactionService(
                repository,
                new RestProductServiceClient(builder, PRODUCT_URL),
                new RestBankServiceClient(builder, BANK_URL),
                new RestPortfolioServiceClient(builder, PORTFOLIO_URL));

        TradeTransactionResponse response = service.create(new CreateTradeTransactionRequest(
                1L, 9L, 1L, 11L, "BUY", new BigDecimal("2.00"), BigDecimal.ONE));

        assertEquals("COMPLETED", response.transactionStatus());
        assertEquals(new BigDecimal("301.0000"), response.totalAmount());
        server.verify();
    }
}
