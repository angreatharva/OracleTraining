package com.example.wtms.Entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_type")
public class ProductType {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "product_type_id")
    private Long productTypeId;

    @Column(name = "type_code")
    private String typeCode;

    @Column(name = "type_name")
    private String typeName;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "productType",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private List<InvestmentProduct> investmentProducts = new ArrayList<>();

    public ProductType() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime currentTime = LocalDateTime.now();
        this.createdAt = currentTime;
        this.updatedAt = currentTime;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addInvestmentProduct(InvestmentProduct investmentProduct) {
        investmentProducts.add(investmentProduct);
        investmentProduct.setProductType(this);
    }

    public void removeInvestmentProduct(InvestmentProduct investmentProduct) {
        investmentProducts.remove(investmentProduct);
        investmentProduct.setProductType(null);
    }

    public Long getProductTypeId() {
        return productTypeId;
    }

    public void setProductTypeId(Long productTypeId) {
        this.productTypeId = productTypeId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<InvestmentProduct> getInvestmentProducts() {
        return investmentProducts;
    }

    public void setInvestmentProducts(
            List<InvestmentProduct> investmentProducts) {
        this.investmentProducts = investmentProducts;
    }
}