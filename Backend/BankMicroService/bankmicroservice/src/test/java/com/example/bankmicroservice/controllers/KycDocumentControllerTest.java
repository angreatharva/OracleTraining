package com.example.bankmicroservice.controllers;

import com.example.bankmicroservice.dto.response.KycDocumentResponse;
import com.example.bankmicroservice.security.AuthorizationHelper;
import com.example.bankmicroservice.security.JwtAuthenticationFilter;
import com.example.bankmicroservice.services.IKycDocumentService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test for the controller's HTTP contract only - security is disabled here, see
 * {@code BankAccountControllerTest} for the rationale.
 */
@WebMvcTest(controllers = KycDocumentController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class KycDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IKycDocumentService kycDocumentService;

    @MockBean
    private AuthorizationHelper authorizationHelper;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createReturns201() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        KycDocumentResponse response = new KycDocumentResponse(
                1L,
                10L,
                "PAN",
                "******234F",
                "pan.pdf",
                "PENDING",
                LocalDate.now(),
                null,
                "ACTIVE",
                now,
                now
        );
        when(kycDocumentService.create(any())).thenReturn(response);

        String request = """
                {
                  "userId": 10,
                  "documentType": "PAN",
                  "documentNumber": "ABCDE1234F",
                  "fileName": "pan.pdf"
                }
                """;

        mockMvc.perform(post("/api/kyc-documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.kycDocumentId").value(1))
                .andExpect(jsonPath("$.verificationStatus").value("PENDING"));
    }
}
