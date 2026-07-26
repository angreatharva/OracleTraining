package com.example.portfoliomicroservice.repositories;

import com.example.portfoliomicroservice.entities.PortfolioAccount;
import com.example.portfoliomicroservice.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioAccountRepository extends JpaRepository<PortfolioAccount, Long> {
    Optional<PortfolioAccount> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    List<PortfolioAccount> findByAccountStatus(AccountStatus accountStatus);
}
