package com.example.wtms.Repositories;

import com.example.wtms.Entities.ProductType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductTypeRepositoryTest {

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Test
    void savesAndFindsProductTypeByCode() {
        ProductType productType = new ProductType(
                "TEST_EQUITY",
                "Test Equity",
                "Product type created by the repository test",
                "ACTIVE"
        );

        ProductType savedProductType = productTypeRepository.saveAndFlush(productType);

        assertTrue(productTypeRepository.existsByTypeCode("TEST_EQUITY"));
        ProductType foundProductType = productTypeRepository
                .findByTypeCode("TEST_EQUITY")
                .orElseThrow();

        assertEquals(savedProductType.getProductTypeId(), foundProductType.getProductTypeId());
        assertEquals("Test Equity", foundProductType.getTypeName());
    }

    @Test
    void findsProductTypesByStatusAndName() {
        productTypeRepository.saveAndFlush(new ProductType(
                "TEST_BOND", "Test Bond", "Active test bond", "ACTIVE"
        ));
        productTypeRepository.saveAndFlush(new ProductType(
                "TEST_DEPOSIT", "Test Deposit", "Inactive test deposit", "INACTIVE"
        ));

        assertEquals(1, productTypeRepository.findByStatus("ACTIVE").stream()
                .filter(productType -> "TEST_BOND".equals(productType.getTypeCode()))
                .count());
        assertEquals(1, productTypeRepository.findByTypeName("Test Deposit").size());
        assertFalse(productTypeRepository.findByTypeCode("UNKNOWN_TYPE").isPresent());
    }
}
