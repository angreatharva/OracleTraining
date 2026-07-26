package com.example.bankmicroservice.repositories;

import com.example.bankmicroservice.entities.BankAccount;
import com.example.bankmicroservice.enums.AccountType;
import com.example.bankmicroservice.enums.BankAccountStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository
        extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByUserId(Long userId);

    List<BankAccount> findByStatus(BankAccountStatus status);

    List<BankAccount> findByBankName(String bankName);

    List<BankAccount> findByAccountType(AccountType accountType);

    List<BankAccount> findByUserIdAndPrimaryAccountTrue(Long userId);

    boolean existsByAccountNumber(String accountNumber);

    boolean existsByAccountNumberAndIfscCode(
            String accountNumber,
            String ifscCode
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT account
            FROM BankAccount account
            WHERE account.bankAccountId = :id
            """)
    Optional<BankAccount> findByIdForUpdate(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE BankAccount account
            SET account.primaryAccount = false
            WHERE account.userId = :userId
              AND account.bankAccountId <> :excludedId
            """)
    int clearOtherPrimaryAccounts(
            @Param("userId") Long userId,
            @Param("excludedId") Long excludedId
    );
}