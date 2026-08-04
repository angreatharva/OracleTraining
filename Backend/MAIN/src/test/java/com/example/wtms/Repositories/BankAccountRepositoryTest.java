package com.example.wtms.Repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.wtms.Entities.BankAccount;
import com.example.wtms.Entities.Role;
import com.example.wtms.Entities.User;
import com.example.wtms.Exceptions.UserException;
import org.springframework.test.annotation.Rollback;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BankAccountRepositoryTest {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @Rollback(false)
    void createBankAccount_shouldSaveSuccessfully() {
        User user = createTestUser();

        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setBankName("SBI");
        account.setBranchName("Main Branch");
        account.setAccountNumber("ACC" + UUID.randomUUID().toString().substring(0, 8));
        account.setAccountType("SAVINGS");
        account.setIfscCode("SBIN0001234");
        account.setBalance(new BigDecimal("1000.00"));
        account.setPrimaryAccount(true);
        account.setStatus("ACTIVE");

        BankAccount saved = bankAccountRepository.saveAndFlush(account);

        assertNotNull(saved.getBankAccountId());
        assertEquals("SBI", saved.getBankName());
        assertEquals(user.getUserId(), saved.getUser().getUserId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void fetchBankAccountById_shouldReturnBankAccount() {
        User user = createTestUser();

        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setBankName("HDFC");
        account.setBranchName("City Branch");
        account.setAccountNumber("ACC" + UUID.randomUUID().toString().substring(0, 8));
        account.setAccountType("SAVINGS");
        account.setIfscCode("HDFC0005678");
        account.setBalance(new BigDecimal("2500.00"));
        account.setPrimaryAccount(false);
        account.setStatus("ACTIVE");

        BankAccount saved = bankAccountRepository.saveAndFlush(account);

        BankAccount found = bankAccountRepository.findById(saved.getBankAccountId()).orElseThrow();

        assertEquals(saved.getBankAccountId(), found.getBankAccountId());
        assertEquals(saved.getAccountNumber(), found.getAccountNumber());
        assertEquals(saved.getUser().getUserId(), found.getUser().getUserId());
    }

    @Test
    void findByUserId_shouldReturnAllAccountsOfUser() {
        User user = createTestUser();

        BankAccount account1 = new BankAccount();
        account1.setUser(user);
        account1.setBankName("SBI");
        account1.setAccountNumber("ACC" + UUID.randomUUID().toString().substring(0, 8));
        account1.setStatus("ACTIVE");
        bankAccountRepository.saveAndFlush(account1);

        BankAccount account2 = new BankAccount();
        account2.setUser(user);
        account2.setBankName("HDFC");
        account2.setAccountNumber("ACC" + UUID.randomUUID().toString().substring(0, 8));
        account2.setStatus("ACTIVE");
        bankAccountRepository.saveAndFlush(account2);

        assertEquals(2, bankAccountRepository.findByUser_UserId(user.getUserId()).size());
    }

    @Test
    void saveBankAccountWithNullBankName_shouldFail() {
        User user = createTestUser();

        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setBankName(null);
        account.setAccountNumber("ACC" + UUID.randomUUID().toString().substring(0, 8));
        account.setStatus("ACTIVE");

        assertThrows(DataIntegrityViolationException.class, () -> bankAccountRepository.saveAndFlush(account));
    }

    @Test
    void saveBankAccountWithoutUser_shouldFail() {
        BankAccount account = new BankAccount();
        account.setBankName("SBI");
        account.setAccountNumber("ACC" + UUID.randomUUID().toString().substring(0, 8));
        account.setStatus("ACTIVE");

        assertThrows(DataIntegrityViolationException.class, () -> bankAccountRepository.saveAndFlush(account));
    }

    @Test
    void findPrimaryBankAccount_shouldReturnPrimaryAccount() {
        User user = createTestUser();

        BankAccount primary = new BankAccount();
        primary.setUser(user);
        primary.setBankName("SBI");
        primary.setAccountNumber("ACC" + UUID.randomUUID().toString().substring(0, 8));
        primary.setPrimaryAccount(true);
        primary.setStatus("ACTIVE");
        bankAccountRepository.saveAndFlush(primary);

        BankAccount secondary = new BankAccount();
        secondary.setUser(user);
        secondary.setBankName("HDFC");
        secondary.setAccountNumber("ACC" + UUID.randomUUID().toString().substring(0, 8));
        secondary.setPrimaryAccount(false);
        secondary.setStatus("ACTIVE");
        bankAccountRepository.saveAndFlush(secondary);

        assertEquals(1, bankAccountRepository.findByUser_UserIdAndPrimaryAccountTrue(user.getUserId()).size());
        assertEquals("SBI",
                bankAccountRepository.findByUser_UserIdAndPrimaryAccountTrue(user.getUserId()).get(0).getBankName());
    }

    private User createTestUser() {
        Role role = new Role();
        role.setRoleName("BANK_ROLE_" + UUID.randomUUID().toString().substring(0, 8));
        Role savedRole = roleRepository.saveAndFlush(role);

        User user = new User();
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setFullName("Bank Test User");
        user.setEmail("bankuser" + UUID.randomUUID().toString().substring(0, 8) + "@mail.com");
        user.setPasswordHash("secret");
        user.setPhone("9999999999");
        user.setStatus("ACTIVE");
        user.setRole(savedRole);

        return userRepository.saveAndFlush(user);
    }
}
