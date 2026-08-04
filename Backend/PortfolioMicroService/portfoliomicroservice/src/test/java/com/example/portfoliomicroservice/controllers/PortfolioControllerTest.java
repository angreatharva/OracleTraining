package com.example.portfoliomicroservice.controllers;

import com.example.portfoliomicroservice.dto.request.CreatePortfolioAccountRequest;
import com.example.portfoliomicroservice.dto.response.PortfolioAccountResponse;
import com.example.portfoliomicroservice.enums.AccountStatus;
import com.example.portfoliomicroservice.security.AuthorizationHelper;
import com.example.portfoliomicroservice.security.JwtAuthenticationFilter;
import com.example.portfoliomicroservice.services.PortfolioService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test for the controller's HTTP contract only.
 *
 * <p>Security is switched off here: {@code addFilters = false} skips the JWT filter chain and
 * {@link AuthorizationHelper} is mocked, so its assert* methods do nothing and the test stays
 * about request mapping and status codes.</p>
 */
@WebMvcTest(controllers = PortfolioController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PortfolioService portfolioService;

    /** Mocked: default no-op behaviour means "allowed". */
    @MockBean
    private AuthorizationHelper authorizationHelper;

    /** Satisfies SecurityConfig's constructor; never invoked because filters are disabled. */
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createAccount_shouldReturnCreatedPortfolio() throws Exception {
        CreatePortfolioAccountRequest request =
                new CreatePortfolioAccountRequest(
                        2L,
                        LocalDate.of(2026, 7, 25)
                );

        PortfolioAccountResponse response = new PortfolioAccountResponse(
                1L,
                2L,
                AccountStatus.ACTIVE,
                LocalDate.of(2026, 7, 25),
                null,
                LocalDateTime.of(2026, 7, 25, 10, 0),
                LocalDateTime.of(2026, 7, 25, 10, 0)
        );

        when(portfolioService.createAccount(any(CreatePortfolioAccountRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.portfolioAccountId").value(1))
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.openedDate").value("2026-07-25"));
    }

    @Test
    void getAccount_shouldReturnPortfolioById() throws Exception {
        PortfolioAccountResponse response = new PortfolioAccountResponse(
                1L,
                2L,
                AccountStatus.ACTIVE,
                LocalDate.of(2026, 7, 25),
                null,
                LocalDateTime.of(2026, 7, 25, 10, 0),
                LocalDateTime.of(2026, 7, 25, 10, 0)
        );

        when(portfolioService.getAccount(1L)).thenReturn(response);

        mockMvc.perform(get("/api/portfolios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioAccountId").value(1))
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.openedDate").value("2026-07-25"));
    }
}