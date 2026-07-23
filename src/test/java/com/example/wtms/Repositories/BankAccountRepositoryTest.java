package com.example.wtms;

import com.example.wtms.Entities.BankAccount;
import com.example.wtms.Repositories.BankAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BankAccountRepositoryTest {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Test
    void shouldSaveAndFindBankAccountById() {
        BankAccount account = createBankAccount(101L, "State Bank", "1234567890", true);

        BankAccount savedAccount = bankAccountRepository.saveAndFlush(account);

        Optional<BankAccount> result = bankAccountRepository.findById(savedAccount.getBankAccountId());

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(101L);
        assertThat(result.get().getBankName()).isEqualTo("State Bank");
        assertThat(result.get().getCreatedAt()).isNotNull();
        assertThat(result.get().getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindAllAccountsForUser() {
        bankAccountRepository.save(createBankAccount(201L, "Bank One", "ACC-001", true));
        bankAccountRepository.save(createBankAccount(201L, "Bank Two", "ACC-002", false));
        bankAccountRepository.save(createBankAccount(202L, "Bank Three", "ACC-003", true));

        List<BankAccount> accounts = bankAccountRepository.findByUserId(201L);

        assertThat(accounts).hasSize(2);
        assertThat(accounts)
                .extracting(BankAccount::getAccountNumber)
                .containsExactlyInAnyOrder("ACC-001", "ACC-002");
    }

    @Test
    void shouldFindPrimaryAccountForUser() {
        bankAccountRepository.saveAndFlush(
                createBankAccount(301L, "Primary Bank", "PRIMARY-001", true)
        );

        Optional<BankAccount> result =
                bankAccountRepository.findByUserIdAndPrimaryAccountTrue(301L);

        assertThat(result).isPresent();
        assertThat(result.get().getPrimaryAccount()).isTrue();
        assertThat(result.get().getAccountNumber()).isEqualTo("PRIMARY-001");
    }

    @Test
    void shouldCheckWhetherAccountNumberExists() {
        bankAccountRepository.saveAndFlush(
                createBankAccount(401L, "Test Bank", "EXISTS-001", false)
        );

        boolean exists = bankAccountRepository.existsByAccountNumber("EXISTS-001");

        assertThat(exists).isTrue();
    }

    private BankAccount createBankAccount(
            Long userId,
            String bankName,
            String accountNumber,
            boolean primary
    ) {
        BankAccount account = new BankAccount();
        account.setUserId(userId);
        account.setBankName(bankName);
        account.setBranchName("Main Branch");
        account.setAccountNumber(accountNumber);
        account.setAccountType("SAVINGS");
        account.setIfscCode("TEST0001234");
        account.setBalance(new BigDecimal("15000.50"));
        account.setPrimaryAccount(primary);
        account.setStatus("ACTIVE");
        return account;
    }
}