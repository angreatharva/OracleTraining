package com.example.portfoliomicroservice.services;

import com.example.portfoliomicroservice.clients.ProductServiceClient;
import com.example.portfoliomicroservice.clients.UserServiceClient;
import com.example.portfoliomicroservice.dto.request.CreateHoldingRequest;
import com.example.portfoliomicroservice.dto.request.CreatePortfolioAccountRequest;
import com.example.portfoliomicroservice.entities.PortfolioAccount;
import com.example.portfoliomicroservice.entities.PortfolioHolding;
import com.example.portfoliomicroservice.enums.AccountStatus;
import com.example.portfoliomicroservice.repositories.PortfolioAccountRepository;
import com.example.portfoliomicroservice.repositories.PortfolioHoldingRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceImplTest {

    @Mock PortfolioAccountRepository accountRepository;
    @Mock PortfolioHoldingRepository holdingRepository;
    @Mock UserServiceClient userServiceClient;
    @Mock ProductServiceClient productServiceClient;
    @InjectMocks PortfolioServiceImpl service;

    @Test
    void createsAccount() {
        when(accountRepository.existsByUserId(10L)).thenReturn(false);
        when(accountRepository.save(any(PortfolioAccount.class))).thenAnswer(invocation -> {
            PortfolioAccount account = invocation.getArgument(0);
            account.setPortfolioAccountId(1L);
            account.setAccountStatus(AccountStatus.ACTIVE);
            return account;
        });

        var response = service.createAccount(new CreatePortfolioAccountRequest(10L, LocalDate.of(2026, 1, 1)));

        assertEquals(1L, response.portfolioAccountId());
        assertEquals(10L, response.userId());
        verify(userServiceClient).validateUser(10L);
    }

    @Test
    void rejectsDuplicateAccount() {
        when(accountRepository.existsByUserId(10L)).thenReturn(true);
        assertThrows(RuntimeException.class,
                () -> service.createAccount(new CreatePortfolioAccountRequest(10L, null)));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void addsHolding() {
        PortfolioAccount account = new PortfolioAccount();
        account.setPortfolioAccountId(1L);
        account.setUserId(10L);
        account.setAccountStatus(AccountStatus.ACTIVE);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(holdingRepository.findByPortfolioAccountPortfolioAccountIdAndProductId(1L, 55L)).thenReturn(Optional.empty());
        when(holdingRepository.save(any(PortfolioHolding.class))).thenAnswer(invocation -> {
            PortfolioHolding holding = invocation.getArgument(0);
            holding.setHoldingId(100L);
            return holding;
        });

        var response = service.addHolding(1L, new CreateHoldingRequest(55L, new BigDecimal("10"), new BigDecimal("100")));

        assertEquals(100L, response.holdingId());
        assertEquals(55L, response.productId());
    }
}
