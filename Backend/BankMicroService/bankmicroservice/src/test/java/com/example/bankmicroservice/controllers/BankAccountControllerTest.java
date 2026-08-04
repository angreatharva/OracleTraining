package com.example.bankmicroservice.controllers;

import com.example.bankmicroservice.dto.response.BankAccountResponse;
import com.example.bankmicroservice.security.AuthorizationHelper;
import com.example.bankmicroservice.security.JwtAuthenticationFilter;
import com.example.bankmicroservice.services.IBankAccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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

/**
 * Slice test for the controller's HTTP contract only.
 *
 * <p>Security is deliberately switched off here: {@code addFilters = false} skips the JWT
 * filter chain and {@link AuthorizationHelper} is mocked so its assert* methods do nothing.
 * That keeps this test about request mapping and status codes. The authorization rules
 * themselves are exercised against a running service, not in this slice.</p>
 */
@WebMvcTest(controllers = BankAccountController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class BankAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IBankAccountService bankAccountService;

    /** Mocked: default no-op behaviour means "allowed". */
    @MockBean
    private AuthorizationHelper authorizationHelper;

    /** Satisfies SecurityConfig's constructor; never invoked because filters are disabled. */
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

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
