package com.example.bankmicroservice.services;

import com.example.bankmicroservice.clients.UserServiceClient;
import com.example.bankmicroservice.clients.model.UserValidationResult;
import com.example.bankmicroservice.dto.request.CreateBankAccountRequest;
import com.example.bankmicroservice.dto.request.DebitRequest;
import com.example.bankmicroservice.dto.response.BankAccountResponse;
import com.example.bankmicroservice.dto.response.DebitResult;
import com.example.bankmicroservice.entities.BankAccount;
import com.example.bankmicroservice.enums.AccountType;
import com.example.bankmicroservice.enums.BankAccountStatus;
import com.example.bankmicroservice.repositories.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UserServiceClient userServiceClient;

    private BankAccountService service;

    @BeforeEach
    void setUp() {
        service = new BankAccountService(bankAccountRepository, userServiceClient);
    }

    @Test
    void createMakesFirstAccountPrimary() {
        CreateBankAccountRequest request = new CreateBankAccountRequest(
                10L,
                "HDFC Bank",
                "Pune",
                "1234 5678 9012",
                "savings",
                "HDFC0001234",
                new BigDecimal("1000.00"),
                false
        );
        when(userServiceClient.validateUser(10L))
                .thenReturn(new UserValidationResult(10L, true, true, "ok"));
        when(bankAccountRepository.existsByAccountNumber("123456789012")).thenReturn(false);
        when(bankAccountRepository.findByUserId(10L)).thenReturn(List.of());
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> {
            BankAccount account = invocation.getArgument(0);
            account.setBankAccountId(1L);
            account.setCreatedAt(LocalDateTime.now());
            account.setUpdatedAt(LocalDateTime.now());
            return account;
        });

        BankAccountResponse response = service.create(request);

        assertThat(response.bankAccountId()).isEqualTo(1L);
        assertThat(response.primaryAccount()).isTrue();
        assertThat(response.maskedAccountNumber()).endsWith("9012");
        verify(bankAccountRepository).clearOtherPrimaryAccounts(10L, 1L);
    }

    @Test
    void createFillsInHouseBankDefaultsWhenOnlyUserIdIsGiven() {
        // What onboarding sends: no bank details at all, just who the account is for.
        CreateBankAccountRequest request = new CreateBankAccountRequest(
                20L, null, null, null, null, null, null, null
        );
        when(userServiceClient.validateUser(20L))
                .thenReturn(new UserValidationResult(20L, true, true, "ok"));
        when(bankAccountRepository.existsByAccountNumber(any())).thenReturn(false);
        when(bankAccountRepository.findByUserId(20L)).thenReturn(List.of());
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> {
            BankAccount account = invocation.getArgument(0);
            account.setBankAccountId(2L);
            account.setCreatedAt(LocalDateTime.now());
            account.setUpdatedAt(LocalDateTime.now());
            return account;
        });

        BankAccountResponse response = service.create(request);

        assertThat(response.bankName()).isEqualTo("WealthTrack Bank");
        assertThat(response.ifscCode()).isEqualTo("WTMS0000001");
        assertThat(response.accountType()).isEqualTo("SAVINGS");
        assertThat(response.balance()).isEqualByComparingTo(BigDecimal.ZERO);
        // First account for this user, so it becomes primary even though the request didn't ask.
        assertThat(response.primaryAccount()).isTrue();
        verify(bankAccountRepository).clearOtherPrimaryAccounts(20L, 2L);
    }

    @Test
    void debitReducesBalanceWhenFundsAreAvailable() {
        BankAccount account = activeAccount(new BigDecimal("500.00"));
        when(bankAccountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(bankAccountRepository.save(account)).thenReturn(account);

        DebitResult result = service.debit(
                1L,
                new DebitRequest(new BigDecimal("125.00"), "TRD-100")
        );

        assertThat(result.approved()).isTrue();
        assertThat(account.getBalance()).isEqualByComparingTo("375.00");
    }

    @Test
    void debitReturnsRejectedResultForInsufficientFunds() {
        BankAccount account = activeAccount(new BigDecimal("50.00"));
        when(bankAccountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));

        DebitResult result = service.debit(
                1L,
                new DebitRequest(new BigDecimal("100.00"), "TRD-101")
        );

        assertThat(result.approved()).isFalse();
        assertThat(result.failureReason()).isEqualTo("Insufficient balance");
        assertThat(account.getBalance()).isEqualByComparingTo("50.00");
    }

    private BankAccount activeAccount(BigDecimal balance) {
        return BankAccount.builder()
                .bankAccountId(1L)
                .userId(10L)
                .bankName("HDFC Bank")
                .branchName("Pune")
                .accountNumber("123456789012")
                .accountType(AccountType.SAVINGS)
                .ifscCode("HDFC0001234")
                .balance(balance)
                .primaryAccount(true)
                .status(BankAccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
