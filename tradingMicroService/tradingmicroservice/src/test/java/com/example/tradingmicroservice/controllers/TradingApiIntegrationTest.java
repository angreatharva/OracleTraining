package com.example.tradingmicroservice.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TradingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndRetrieveTradeTransactionShowsApiResults() throws Exception {
        String createRequest = """
                {
                  "portfolioAccountId": 1,
                  "holdingId": 1,
                  "productId": 1,
                  "transactionType": "BUY",
                  "quantity": 2,
                  "unitPrice": 150.50
                }
                """;

        String createResponse = mockMvc.perform(post("/api/trade-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("BUY"))
                .andExpect(jsonPath("$.transactionStatus").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode transactionJson = objectMapper.readTree(createResponse);
        long transactionId = transactionJson.get("transactionId").asLong();

        System.out.println("\n=== TRADE TRANSACTION CREATED ===");
        System.out.println(createResponse);

        mockMvc.perform(get("/api/trade-transactions/{id}", transactionId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(transactionId))
                .andExpect(jsonPath("$.totalAmount").value(301.0));

        System.out.println("=== TRADE TRANSACTION RETRIEVED SUCCESSFULLY ===\n");
    }

    @Test
    void createPortfolioStatementShowsApiResults() throws Exception {
        String transactionResponse = mockMvc.perform(post("/api/trade-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioAccountId": 1,
                                  "holdingId": 1,
                                  "productId": 1,
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

        String statementResponse = mockMvc.perform(post("/api/portfolio-statements/internal")
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
