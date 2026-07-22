package com.example.wtms.Repositories;


import com.example.wtms.Entities.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {

    Optional<UserDetails> findByUser_UserId(Long userId);

    Optional<UserDetails> findByKycStatus(String kycStatus);

    Optional<UserDetails> findByRiskLevel(String riskLevel);
}