package com.example.wtms.Repositories;

import com.example.wtms.Entities.PortfolioAccount;
import com.example.wtms.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioAccountRepository extends JpaRepository<PortfolioAccount, Long> {

    Optional<PortfolioAccount> findByUser(User user);

    Optional<PortfolioAccount> findByUserUserId(Long userId);

    boolean existsByUserUserId(Long userId);
}