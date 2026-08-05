package com.example.bankmicroservice.services;

import com.example.bankmicroservice.clients.UserServiceClient;
import com.example.bankmicroservice.clients.model.UserValidationResult;
import com.example.bankmicroservice.dto.request.CreateBankAccountRequest;
import com.example.bankmicroservice.dto.request.CreditRequest;
import com.example.bankmicroservice.dto.request.DebitRequest;
import com.example.bankmicroservice.dto.request.UpdateBankAccountRequest;
import com.example.bankmicroservice.dto.response.BankAccountResponse;
import com.example.bankmicroservice.dto.response.CreditResult;
import com.example.bankmicroservice.dto.response.DebitResult;
import com.example.bankmicroservice.entities.BankAccount;
import com.example.bankmicroservice.enums.AccountType;
import com.example.bankmicroservice.enums.BankAccountStatus;
import com.example.bankmicroservice.exceptions.BankAccountNotFoundException;
import com.example.bankmicroservice.exceptions.DuplicateBankAccountException;
import com.example.bankmicroservice.exceptions.UserValidationException;
import com.example.bankmicroservice.repositories.BankAccountRepository;
import com.example.bankmicroservice.utils.EnumParser;
import com.example.bankmicroservice.utils.MaskingUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
@Transactional
public class BankAccountService implements IBankAccountService {

    /**
     * Used when onboarding opens an account automatically and the caller supplied no real
     * bank details. This is not a real correspondent bank - it is WealthTrack acting as its
     * own custodian for a new investor until the manager (or investor) links a real one via
     * {@link #update}.
     */
    private static final String DEFAULT_BANK_NAME = "WealthTrack Bank";
    private static final String DEFAULT_IFSC_CODE = "WTMS0000001";
    private static final String ACCOUNT_NUMBER_PREFIX = "WT";
    private static final int MAX_ACCOUNT_NUMBER_ATTEMPTS = 10;

    private final BankAccountRepository bankAccountRepository;
    private final UserServiceClient userServiceClient;
    private final SecureRandom random = new SecureRandom();

    public BankAccountService(
            BankAccountRepository bankAccountRepository,
            UserServiceClient userServiceClient) {
        this.bankAccountRepository = bankAccountRepository;
        this.userServiceClient = userServiceClient;
    }

    @Override
    public BankAccountResponse create(CreateBankAccountRequest request) {
        validateUser(request.userId());

        String normalizedAccountNumber = isBlank(request.accountNumber())
                ? generateAccountNumber(request.userId())
                : normalize(request.accountNumber());

        if (bankAccountRepository.existsByAccountNumber(normalizedAccountNumber)) {
            throw new DuplicateBankAccountException(normalizedAccountNumber);
        }

        AccountType accountType = isBlank(request.accountType())
                ? AccountType.SAVINGS
                : EnumParser.parse(request.accountType(), AccountType.class, "accountType");

        String bankName = isBlank(request.bankName()) ? DEFAULT_BANK_NAME : request.bankName().trim();
        String ifscCode = upperToNull(request.ifscCode());

        boolean hasNoAccounts = bankAccountRepository.findByUserId(request.userId()).isEmpty();
        boolean shouldBePrimary = Boolean.TRUE.equals(request.primaryAccount()) || hasNoAccounts;

        BankAccount account = BankAccount.builder()
                .userId(request.userId())
                .bankName(bankName)
                .branchName(trimToNull(request.branchName()))
                .accountNumber(normalizedAccountNumber)
                .accountType(accountType)
                .ifscCode(ifscCode == null ? DEFAULT_IFSC_CODE : ifscCode)
                .balance(request.openingBalance() == null ? BigDecimal.ZERO : request.openingBalance())
                .primaryAccount(shouldBePrimary)
                .status(BankAccountStatus.ACTIVE)
                .build();

        BankAccount saved = bankAccountRepository.save(account);
        if (shouldBePrimary) {
            bankAccountRepository.clearOtherPrimaryAccounts(saved.getUserId(), saved.getBankAccountId());
        }
        return toResponse(saved);
    }

    /**
     * House-bank account numbers are internally generated, never entered by a person, so they
     * only need to be unique - not meaningful. Retries on collision rather than trusting
     * randomness alone, since {@code account_number} is a unique column.
     */
    private String generateAccountNumber(Long userId) {
        for (int attempt = 0; attempt < MAX_ACCOUNT_NUMBER_ATTEMPTS; attempt++) {
            String candidate = ACCOUNT_NUMBER_PREFIX + userId + String.format("%06d", random.nextInt(1_000_000));
            if (!bankAccountRepository.existsByAccountNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not generate a unique account number for user " + userId);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Override
    @Transactional(readOnly = true)
    public BankAccountResponse getById(Long id) {
        return toResponse(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountResponse> getAll(Long userId, String status, Boolean primary) {
        BankAccountStatus parsedStatus = status == null
                ? null
                : EnumParser.parse(status, BankAccountStatus.class, "status");

        Stream<BankAccount> accounts = userId == null
                ? bankAccountRepository.findAll().stream()
                : bankAccountRepository.findByUserId(userId).stream();

        return accounts
                .filter(account -> parsedStatus == null || account.getStatus() == parsedStatus)
                .filter(account -> primary == null || account.getPrimaryAccount().equals(primary))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public BankAccountResponse update(Long id, UpdateBankAccountRequest request) {
        BankAccount account = getEntityById(id);

        if (request.bankName() != null) {
            account.setBankName(request.bankName().trim());
        }
        if (request.branchName() != null) {
            account.setBranchName(trimToNull(request.branchName()));
        }
        if (request.accountType() != null) {
            account.setAccountType(EnumParser.parse(request.accountType(), AccountType.class, "accountType"));
        }
        if (request.ifscCode() != null) {
            account.setIfscCode(upperToNull(request.ifscCode()));
        }
        if (request.status() != null) {
            BankAccountStatus newStatus = EnumParser.parse(
                    request.status(), BankAccountStatus.class, "status");
            account.setStatus(newStatus);
            if (newStatus == BankAccountStatus.CLOSED) {
                account.setPrimaryAccount(false);
            }
        }

        return toResponse(bankAccountRepository.save(account));
    }

    @Override
    public BankAccountResponse makePrimary(Long id) {
        BankAccount account = getEntityById(id);
        if (account.getStatus() != BankAccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Only an ACTIVE bank account can be primary");
        }
        account.setPrimaryAccount(true);
        bankAccountRepository.save(account);
        bankAccountRepository.clearOtherPrimaryAccounts(account.getUserId(), account.getBankAccountId());
        return toResponse(account);
    }

    @Override
    public DebitResult debit(Long id, DebitRequest request) {
        BankAccount account = bankAccountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BankAccountNotFoundException(id));

        if (account.getStatus() != BankAccountStatus.ACTIVE) {
            return new DebitResult(false, request.transactionReference(),
                    "Bank account is not ACTIVE");
        }
        if (account.getBalance().compareTo(request.amount()) < 0) {
            return new DebitResult(false, request.transactionReference(),
                    "Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(request.amount()));
        bankAccountRepository.save(account);
        return new DebitResult(true, request.transactionReference(), null);
    }

    @Override
    public CreditResult credit(Long id, CreditRequest request) {
        BankAccount account = bankAccountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BankAccountNotFoundException(id));

        if (account.getStatus() == BankAccountStatus.CLOSED) {
            return new CreditResult(false, request.transactionReference(),
                    "Cannot credit a CLOSED bank account");
        }

        account.setBalance(account.getBalance().add(request.amount()));
        bankAccountRepository.save(account);
        return new CreditResult(true, request.transactionReference(), null);
    }

    @Override
    public void close(Long id) {
        BankAccount account = getEntityById(id);
        account.setStatus(BankAccountStatus.CLOSED);
        account.setPrimaryAccount(false);
        bankAccountRepository.save(account);
    }

    private BankAccount getEntityById(Long id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException(id));
    }

    private void validateUser(Long userId) {
        UserValidationResult validation = userServiceClient.validateUser(userId);
        if (validation == null || !validation.usable()) {
            String reason = validation == null ? "User service returned no result" : validation.message();
            throw new UserValidationException(userId, reason);
        }
    }

    private BankAccountResponse toResponse(BankAccount account) {
        return new BankAccountResponse(
                account.getBankAccountId(),
                account.getUserId(),
                account.getBankName(),
                account.getBranchName(),
                MaskingUtils.mask(account.getAccountNumber()),
                account.getAccountType().name(),
                account.getIfscCode(),
                account.getBalance(),
                Boolean.TRUE.equals(account.getPrimaryAccount()),
                account.getStatus().name(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    private String normalize(String value) {
        return value.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String upperToNull(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase(Locale.ROOT);
    }
}
