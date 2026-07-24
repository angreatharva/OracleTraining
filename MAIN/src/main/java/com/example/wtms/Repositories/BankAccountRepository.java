package com.example.wtms.Repositories;

import com.example.wtms.Entities.BankAccount;
import com.example.wtms.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByUser(User user);

    List<BankAccount> findByUser_UserId(Long userId);

    List<BankAccount> findByStatus(String status);

    List<BankAccount> findByBankName(String bankName);

    List<BankAccount> findByAccountType(String accountType);

    List<BankAccount> findByUser_UserIdAndPrimaryAccountTrue(Long userId);

    boolean existsByAccountNumber(String accountNumber);

    boolean existsByAccountNumberAndIfscCode(String accountNumber, String ifscCode);
}