package com.example.wtms.Repositories;


import com.example.wtms.Entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository
        extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByUserId(Long userId);

    List<BankAccount> findByUserIdAndStatus(
            Long userId,
            String status
    );

    Optional<BankAccount> findByAccountNumber(String accountNumber);

    Optional<BankAccount> findByUserIdAndPrimaryAccountTrue(Long userId);

    boolean existsByAccountNumber(String accountNumber);
}
