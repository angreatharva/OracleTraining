package com.example.tradingmicroservice.controllers;

import com.example.tradingmicroservice.clients.BankServiceClient;
import com.example.tradingmicroservice.clients.PortfolioServiceClient;
import com.example.tradingmicroservice.clients.ProductServiceClient;
import com.example.tradingmicroservice.clients.model.CreditResult;
import com.example.tradingmicroservice.clients.model.DebitResult;
import com.example.tradingmicroservice.clients.model.ProductQuote;
import com.example.commonsecurity.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TradingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductServiceClient productServiceClient;

    @MockBean
    private BankServiceClient bankServiceClient;

    @MockBean
    private PortfolioServiceClient portfolioServiceClient;

    @Autowired
    private JwtService jwtService;

    /**
     * These tests drive the API as a trusted internal caller.
     *
     * <p>A SERVICE token is used rather than an investor one because the endpoints exercised
     * here include {@code POST /api/portfolio-statements/internal}, which is SERVICE-only by
     * design. It also keeps the test focused on the trade saga instead of on ownership
     * resolution, which would otherwise need the mocked Portfolio client to answer
     * {@code getAccount} for every request.</p>
     */
    private String serviceToken;

    @BeforeEach
    void issueServiceToken() {
        serviceToken = jwtService.generateServiceToken("TRADING-TEST", Duration.ofMinutes(5));
    }

    /** Adds the bearer token every secured endpoint now requires. */
    private MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceToken);
    }

    @Test
    void createAndRetrieveCompletedBuyTradeShowsApiResults() throws Exception {
        when(productServiceClient.getActiveProduct(1L))
                .thenReturn(new ProductQuote(1L, "Test product", new java.math.BigDecimal("150.50"), true));
        when(bankServiceClient.authorizeDebit(eq(11L), any()))
                .thenReturn(new DebitResult(true, "TRADE-1", null));
        doNothing().when(portfolioServiceClient).applyCompletedTrade(any());
        String createRequest = """
                {
                  "portfolioAccountId": 1,
                  "holdingId": 1,
                  "productId": 1,
                  "bankAccountId": 11,
                  "transactionType": "BUY",
                  "quantity": 2,
                  "unitPrice": 150.50
                }
                """;

        String createResponse = mockMvc.perform(authorized(post("/api/trade-transactions"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("BUY"))
                .andExpect(jsonPath("$.transactionStatus").value("COMPLETED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode transactionJson = objectMapper.readTree(createResponse);
        long transactionId = transactionJson.get("transactionId").asLong();

        System.out.println("\n=== TRADE TRANSACTION CREATED ===");
        System.out.println(createResponse);

        mockMvc.perform(authorized(get("/api/trade-transactions/{id}", transactionId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(transactionId))
                .andExpect(jsonPath("$.totalAmount").value(301.0));

        System.out.println("=== TRADE TRANSACTION RETRIEVED SUCCESSFULLY ===\n");
    }

    @Test
    void createPortfolioStatementShowsApiResults() throws Exception {
        when(productServiceClient.getActiveProduct(1L))
                .thenReturn(new ProductQuote(1L, "Test product", new java.math.BigDecimal("200.00"), true));
        when(bankServiceClient.credit(eq(11L), any()))
                .thenReturn(new CreditResult(true, "TRADE-1", null));
        doNothing().when(portfolioServiceClient).applyCompletedTrade(any());
        String transactionResponse = mockMvc.perform(authorized(post("/api/trade-transactions"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioAccountId": 1,
                                  "holdingId": 1,
                                  "productId": 1,
                                  "bankAccountId": 11,
                                  "transactionType": "SELL",
                                  "quantity": 1,
                                  "unitPrice": 200.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long transactionId = objectMapper.readTree(transactionResponse).get("transactionId").asLong();
        String statementRequest = """
                {
                  "portfolioAccountId": 1,
                  "holdingId": 1,
                  "transactionId": %d,
                  "statementStart": "2026-07-01",
                  "statementEnd": "2026-07-31",
                  "openingValue": 1000.00,
                  "closingValue": 1200.00,
                  "transactionIds": [%d]
                }
                """.formatted(transactionId, transactionId);

        String statementResponse = mockMvc.perform(authorized(post("/api/portfolio-statements/internal"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statementRequest))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(transactionId))
                .andExpect(jsonPath("$.status").value("GENERATED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        System.out.println("\n=== PORTFOLIO STATEMENT CREATED ===");
        System.out.println(statementResponse);
        System.out.println("=== PORTFOLIO STATEMENT LINKED TO TRADE SUCCESSFULLY ===\n");
    }
}
