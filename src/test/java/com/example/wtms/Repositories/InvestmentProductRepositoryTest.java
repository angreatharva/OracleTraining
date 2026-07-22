package com.example.wtms.Repositories;

import com.example.wtms.Entities.InvestmentProduct;
import com.example.wtms.Entities.ProductType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InvestmentProductRepositoryTest {

    @Autowired
    private InvestmentProductRepository investmentProductRepository;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    private InvestmentProduct product(
            ProductType productType,
            String productName,
            String status,
            String riskCategory,
            String priceMethod
    ) {
        return new InvestmentProduct(
                productType,
                productName,
                new BigDecimal("100.00"),
                new BigDecimal("125.00"),
                new BigDecimal("1000.00"),
                riskCategory,
                priceMethod,
                12,
                new BigDecimal("7.50"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 1),
                status
        );
    }


    @Test
    void savesAndFindsProductsByProductType() {
        ProductType productType = productTypeRepository.saveAndFlush(new ProductType(
                "TEST_MUTUAL_FUND", "Test Mutual Fund", "Repository test type", "ACTIVE"
        ));

        InvestmentProduct product = investmentProductRepository.saveAndFlush(product(
                productType, "Test Growth Fund", "ACTIVE", "HIGH", "NAV"
        ));

        var products = investmentProductRepository
                .findByProductType_ProductTypeId(productType.getProductTypeId());

        assertEquals(1, products.stream()
                .filter(foundProduct -> foundProduct.getProductId().equals(product.getProductId()))
                .count());
        assertEquals("Test Growth Fund", products.get(0).getProductName());
    }

    @Test
    void findsProductsByNameStatusRiskCategoryAndPriceMethod() {
        ProductType productType = productTypeRepository.saveAndFlush(new ProductType(
                "TEST_PRODUCT_FILTER", "Test Product Filter", "Repository test type", "ACTIVE"
        ));

        investmentProductRepository.saveAndFlush(product(
                productType, "Test High Risk Fund", "ACTIVE", "HIGH", "NAV"
        ));
        investmentProductRepository.saveAndFlush(product(
                productType, "Test Fixed Deposit", "INACTIVE", "LOW", "FIXED"
        ));

        assertEquals(1, investmentProductRepository.findByProductName("Test High Risk Fund").size());
        assertEquals(1, investmentProductRepository.findByStatus("ACTIVE").stream()
                .filter(product -> "Test High Risk Fund".equals(product.getProductName()))
                .count());
        assertEquals(1, investmentProductRepository.findByRiskCategory("LOW").stream()
                .filter(product -> "Test Fixed Deposit".equals(product.getProductName()))
                .count());
        assertTrue(investmentProductRepository.findByPriceMethod("NAV").stream()
                .anyMatch(product -> "Test High Risk Fund".equals(product.getProductName())));
    }


}