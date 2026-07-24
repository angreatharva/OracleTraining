package com.example.wtms.Entities;
import com.example.wtms.Entities.ProductType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "investment_product")
public class InvestmentProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_type_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_investment_product_product_type"
            )
    )
    private ProductType productType;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "current_price")
    private BigDecimal currentPrice;

    @Column(name = "minimum_investment")
    private BigDecimal minimumInvestment;

    @Column(name = "risk_category")
    private String riskCategory;

    @Column(name = "price_method")
    private String priceMethod;

    @Column(name = "tenure_months")
    private Integer tenureMonths;

    @Column(name = "interest_rate")
    private BigDecimal interestRate;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public InvestmentProduct() {
    }

    public InvestmentProduct(
            ProductType productType,
            String productName,
            BigDecimal basePrice,
            BigDecimal currentPrice,
            BigDecimal minimumInvestment,
            String riskCategory,
            String priceMethod,
            Integer tenureMonths,
            BigDecimal interestRate,
            LocalDate issueDate,
            LocalDate maturityDate,
            String status
    ) {
        this.productType = productType;
        this.productName = productName;
        this.basePrice = basePrice;
        this.currentPrice = currentPrice;
        this.minimumInvestment = minimumInvestment;
        this.riskCategory = riskCategory;
        this.priceMethod = priceMethod;
        this.tenureMonths = tenureMonths;
        this.interestRate = interestRate;
        this.issueDate = issueDate;
        this.maturityDate = maturityDate;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getMinimumInvestment() {
        return minimumInvestment;
    }

    public void setMinimumInvestment(
            BigDecimal minimumInvestment
    ) {
        this.minimumInvestment = minimumInvestment;
    }

    public String getRiskCategory() {
        return riskCategory;
    }

    public void setRiskCategory(String riskCategory) {
        this.riskCategory = riskCategory;
    }

    public String getPriceMethod() {
        return priceMethod;
    }

    public void setPriceMethod(String priceMethod) {
        this.priceMethod = priceMethod;
    }

    public Integer getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(Integer tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(LocalDate maturityDate) {
        this.maturityDate = maturityDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
