package com.example.wtms.Repositories;


import com.example.wtms.Entities.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetail, Long> {

    Optional<UserDetail> findByUser_UserId(Long userId);

    Optional<UserDetail> findByKycStatus(String kycStatus);

    Optional<UserDetail> findByRiskLevel(String riskLevel);
}