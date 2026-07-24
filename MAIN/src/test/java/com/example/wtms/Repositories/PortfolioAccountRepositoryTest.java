package com.example.wtms.Repositories;

import com.example.wtms.Entities.PortfolioAccount;
import com.example.wtms.Entities.Role;
import com.example.wtms.Entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PortfolioAccountRepositoryTest {

    @Autowired
    private PortfolioAccountRepository portfolioAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void testFindByUser() {
        Role role = new Role();
        role.setRoleName("PORTFOLIO_ROLE_" + UUID.randomUUID().toString().substring(0, 8));
        role = roleRepository.save(role);

        User user = new User();
        user.setFullName("Anjali");
        user.setEmail("anjali." + UUID.randomUUID().toString().substring(0, 8) + "@test.com");
        user.setPasswordHash("123");
        user.setRole(role);
        user = userRepository.save(user);

        PortfolioAccount account = new PortfolioAccount();
        account.setUser(user);
        account.setAccountStatus("ACTIVE");
        account.setOpenedDate(LocalDate.now());
        account.setStatus("ACTIVE");
        portfolioAccountRepository.save(account);

        PortfolioAccount result = portfolioAccountRepository.findByUser(user).orElse(null);

        assertNotNull(result);
        assertEquals(user.getUserId(), result.getUser().getUserId());
    }

    @Test
    void testExistsByUserUserId() {
        Role role = new Role();
        role.setRoleName("PORTFOLIO_ROLE_" + UUID.randomUUID().toString().substring(0, 8));
        role = roleRepository.save(role);

        User user = new User();
        user.setFullName("Test");
        user.setEmail("test." + UUID.randomUUID().toString().substring(0, 8) + "@test.com");
        user.setPasswordHash("123");
        user.setRole(role);
        user = userRepository.save(user);

        PortfolioAccount account = new PortfolioAccount();
        account.setUser(user);
        account.setAccountStatus("ACTIVE");
        account.setOpenedDate(LocalDate.now());
        portfolioAccountRepository.save(account);

        assertTrue(portfolioAccountRepository.existsByUserUserId(user.getUserId()));
    }
}
