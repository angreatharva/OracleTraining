package com.example.wtms.Repositories;

import com.example.wtms.Entities.Role;
import com.example.wtms.Entities.User;
import com.example.wtms.Entities.ProductType;
import com.example.wtms.Entities.PortfolioAccount;
import com.example.wtms.Entities.PortfolioHolding;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PortfolioHoldingRepositoryTest {

    @Autowired
    private PortfolioHoldingRepository portfolioHoldingRepository;

    @Autowired
    private PortfolioAccountRepository portfolioAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Test
    void testFindByPortfolioAccountPortfolioAccountId() {

        Role role = new Role();
        role.setRoleName("HOLDING_ROLE_" + UUID.randomUUID().toString().substring(0, 8));
        role = roleRepository.save(role);

        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("john." + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        user.setPasswordHash("password");
        user.setPhone("9876543210");
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setRole(role);
        user = userRepository.save(user);

        PortfolioAccount account = new PortfolioAccount();
        account.setUser(user);
        account.setAccountStatus("ACTIVE");
        account.setOpenedDate(LocalDate.now());
        account.setStatus("ACTIVE");
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        account = portfolioAccountRepository.save(account);

        ProductType product = new ProductType();
        product.setTypeCode("MF_" + UUID.randomUUID().toString().substring(0, 8));
        product.setTypeName("Mutual Fund");
        product.setDescription("Mutual Fund");
        product.setStatus("ACTIVE");
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product = productTypeRepository.save(product);

        PortfolioHolding holding = new PortfolioHolding();
        holding.setPortfolioAccount(account);
        holding.setProduct(product);
        holding.setQuantity(BigDecimal.valueOf(10));
        holding.setAverageCost(BigDecimal.valueOf(100));
        holding.setMarketValue(BigDecimal.valueOf(1000));
        holding.setHoldingStatus("ACTIVE");
        holding.setCreatedAt(LocalDateTime.now());
        holding.setUpdatedAt(LocalDateTime.now());
        portfolioHoldingRepository.save(holding);

        List<PortfolioHolding> holdings =
                portfolioHoldingRepository.findByPortfolioAccountPortfolioAccountId(
                        account.getPortfolioAccountId());

        assertNotNull(holdings);
        assertEquals(1, holdings.size());
        assertEquals("ACTIVE", holdings.get(0).getHoldingStatus());
        assertEquals(product.getProductTypeId(),
                holdings.get(0).getProduct().getProductTypeId());
    }

    @Test
    void testFindByProductProductTypeId() {

        Role role = new Role();
        role.setRoleName("HOLDING_ROLE_" + UUID.randomUUID().toString().substring(0, 8));
        role = roleRepository.save(role);

        User user = new User();
        user.setFullName("Jane Doe");
        user.setEmail("jane." + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        user.setPasswordHash("password");
        user.setPhone("9876543211");
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setRole(role);
        user = userRepository.save(user);

        PortfolioAccount account = new PortfolioAccount();
        account.setUser(user);
        account.setAccountStatus("ACTIVE");
        account.setOpenedDate(LocalDate.now());
        account.setStatus("ACTIVE");
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        account = portfolioAccountRepository.save(account);

        ProductType product = new ProductType();
        product.setTypeCode("EQ_" + UUID.randomUUID().toString().substring(0, 8));
        product.setTypeName("Equity");
        product.setDescription("Equity Product");
        product.setStatus("ACTIVE");
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product = productTypeRepository.save(product);

        PortfolioHolding holding = new PortfolioHolding();
        holding.setPortfolioAccount(account);
        holding.setProduct(product);
        holding.setQuantity(BigDecimal.valueOf(5));
        holding.setAverageCost(BigDecimal.valueOf(200));
        holding.setMarketValue(BigDecimal.valueOf(1000));
        holding.setHoldingStatus("ACTIVE");
        holding.setCreatedAt(LocalDateTime.now());
        holding.setUpdatedAt(LocalDateTime.now());
        portfolioHoldingRepository.save(holding);

        List<PortfolioHolding> holdings =
                portfolioHoldingRepository.findByProductProductTypeId(
                        product.getProductTypeId());

        assertNotNull(holdings);
        assertEquals(1, holdings.size());
        assertEquals(product.getProductTypeId(),
                holdings.get(0).getProduct().getProductTypeId());
    }
}
