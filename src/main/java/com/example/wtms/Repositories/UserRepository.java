package com.example.wtms.Repositories;

import com.example.wtms.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByStatus(String status);

    List<User> findByManager_UserId(Long managerId);

    List<User> findByRole_RoleId(Long roleId);
}