package com.example.bankmicroservice.controllers;

import com.example.bankmicroservice.dto.response.BankAccountResponse;
import com.example.bankmicroservice.services.IBankAccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankAccountController.class)
class BankAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IBankAccountService bankAccountService;

    @Test
    void createReturns201() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        BankAccountResponse response = new BankAccountResponse(
                1L, 10L, "HDFC Bank", "Pune", "********9012", "SAVINGS",
                "HDFC0001234", new BigDecimal("1000.00"), true, "ACTIVE", now, now
        );
        when(bankAccountService.create(any())).thenReturn(response);

        String request = """
                {
                  "userId": 10,
                  "bankName": "HDFC Bank",
                  "branchName": "Pune",
                  "accountNumber": "123456789012",
                  "accountType": "SAVINGS",
                  "ifscCode": "HDFC0001234",
                  "openingBalance": 1000.00,
                  "primaryAccount": true
                }
                """;

        String response1 = mockMvc.perform(post("/api/bank-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bankAccountId").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE")).andReturn().getResponse().getContentAsString();

        System.out.println("\n =========== \n");
        System.out.println("Test pass -> " + response1 );
    }


}
