package com.example.productmicroservice.repositories;

import com.example.productmicroservice.entities.InvestmentProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvestmentProductRepository extends JpaRepository<InvestmentProduct, Long> {

    List<InvestmentProduct> findByProductType_ProductTypeId(Long productTypeId);

    List<InvestmentProduct> findByProductName(String productName);

    List<InvestmentProduct> findByStatus(String status);

    List<InvestmentProduct> findByRiskCategory(String riskCategory);

    List<InvestmentProduct> findByPriceMethod(String priceMethod);

    boolean existsByProductNameIgnoreCase(String productName);

    boolean existsByProductType_ProductTypeId(Long productTypeId);
}
