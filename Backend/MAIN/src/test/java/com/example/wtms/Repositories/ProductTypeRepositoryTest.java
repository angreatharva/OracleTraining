package com.example.wtms.Repositories;

import com.example.wtms.Entities.ProductType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductTypeRepositoryTest {

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Test
    void createProductType() {
        ProductType productType = new ProductType(
                "TEST_EQUITY",
                "Test Equity",
                "Product type created by the repository test",
                "ACTIVE"
        );

        ProductType savedProductType = productTypeRepository.saveAndFlush(productType);

        assertNotNull(savedProductType.getProductTypeId());
        assertTrue(productTypeRepository.existsByTypeCode("TEST_EQUITY"));
    }

    @Test
    void readAllProductTypes() {
        long countBeforeCreate = productTypeRepository.count();
        ProductType savedProductType = productTypeRepository.saveAndFlush(new ProductType(
                "TEST_READ_ALL", "Test Read All", "Product type for read-all test", "ACTIVE"
        ));

        List<ProductType> productTypes = productTypeRepository.findAll();

        assertEquals(countBeforeCreate + 1, productTypes.size());
        assertTrue(productTypes.stream().anyMatch(productType -> productType
                .getProductTypeId().equals(savedProductType.getProductTypeId())));
    }

    @Test
    void readProductTypeById() {
        ProductType savedProductType = productTypeRepository.saveAndFlush(new ProductType(
                "TEST_READ_ID", "Test Read By Id", "Product type for read-by-id test", "ACTIVE"
        ));

        ProductType foundProductType = productTypeRepository
                .findById(savedProductType.getProductTypeId())
                .orElseThrow();

        assertEquals("Test Read By Id", foundProductType.getTypeName());
    }

    @Test
    void updateProductType() {
        ProductType savedProductType = productTypeRepository.saveAndFlush(new ProductType(
                "TEST_UPDATE", "Original Name", "Product type for update test", "INACTIVE"
        ));
        savedProductType.setTypeName("Updated Name");
        savedProductType.setStatus("ACTIVE");
        productTypeRepository.saveAndFlush(savedProductType);

        ProductType foundProductType = productTypeRepository
                .findById(savedProductType.getProductTypeId())
                .orElseThrow();

        assertEquals("Updated Name", foundProductType.getTypeName());
        assertEquals("ACTIVE", foundProductType.getStatus());
    }

    @Test
    void deleteProductType() {
        ProductType savedProductType = productTypeRepository.saveAndFlush(new ProductType(
                "TEST_DELETE", "Test Delete", "Product type for delete test", "ACTIVE"
        ));

        productTypeRepository.deleteById(savedProductType.getProductTypeId());
        productTypeRepository.flush();

        assertFalse(productTypeRepository.existsById(savedProductType.getProductTypeId()));
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
