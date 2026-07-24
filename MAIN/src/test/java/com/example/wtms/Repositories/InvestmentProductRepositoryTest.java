package com.example.wtms.Repositories;

import com.example.wtms.Entities.InvestmentProduct;
import com.example.wtms.Entities.ProductType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    void createInvestmentProduct() {
        ProductType productType = createProductType("TEST_CREATE_PRODUCT");

        InvestmentProduct savedProduct = investmentProductRepository.saveAndFlush(product(
                productType, "Test Create Product", "ACTIVE", "MODERATE", "NAV"
        ));

        assertNotNull(savedProduct.getProductId());
        assertEquals("Test Create Product", savedProduct.getProductName());
    }

    @Test
    void readAllInvestmentProducts() {
        long countBeforeCreate = investmentProductRepository.count();
        ProductType productType = createProductType("TEST_READ_ALL_PRODUCT");
        InvestmentProduct savedProduct = investmentProductRepository.saveAndFlush(product(
                productType, "Test Read All Product", "ACTIVE", "MODERATE", "NAV"
        ));

        List<InvestmentProduct> products = investmentProductRepository.findAll();

        assertEquals(countBeforeCreate + 1, products.size());
        assertTrue(products.stream().anyMatch(product -> product
                .getProductId().equals(savedProduct.getProductId())));
    }

    @Test
    void readInvestmentProductById() {
        ProductType productType = createProductType("TEST_READ_PRODUCT_ID");
        InvestmentProduct savedProduct = investmentProductRepository.saveAndFlush(product(
                productType, "Test Read Product By Id", "ACTIVE", "MODERATE", "NAV"
        ));

        InvestmentProduct foundProduct = investmentProductRepository
                .findById(savedProduct.getProductId())
                .orElseThrow();

        assertEquals("Test Read Product By Id", foundProduct.getProductName());
    }

    @Test
    void updateInvestmentProduct() {
        ProductType productType = createProductType("TEST_UPDATE_PRODUCT");
        InvestmentProduct savedProduct = investmentProductRepository.saveAndFlush(product(
                productType, "Original Product Name", "INACTIVE", "LOW", "FIXED"
        ));
        savedProduct.setProductName("Updated Product Name");
        savedProduct.setCurrentPrice(new BigDecimal("150.00"));
        savedProduct.setStatus("ACTIVE");
        investmentProductRepository.saveAndFlush(savedProduct);

        InvestmentProduct foundProduct = investmentProductRepository
                .findById(savedProduct.getProductId())
                .orElseThrow();

        assertEquals("Updated Product Name", foundProduct.getProductName());
        assertEquals(new BigDecimal("150.00"), foundProduct.getCurrentPrice());
        assertEquals("ACTIVE", foundProduct.getStatus());
    }

    @Test
    void deleteInvestmentProduct() {
        ProductType productType = createProductType("TEST_DELETE_PRODUCT");
        InvestmentProduct savedProduct = investmentProductRepository.saveAndFlush(product(
                productType, "Test Delete Product", "ACTIVE", "MODERATE", "NAV"
        ));

        investmentProductRepository.deleteById(savedProduct.getProductId());
        investmentProductRepository.flush();

        assertFalse(investmentProductRepository.existsById(savedProduct.getProductId()));
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

    private ProductType createProductType(String typeCode) {
        return productTypeRepository.saveAndFlush(new ProductType(
                typeCode, typeCode + " Name", "Repository test type", "ACTIVE"
        ));
    }

}
