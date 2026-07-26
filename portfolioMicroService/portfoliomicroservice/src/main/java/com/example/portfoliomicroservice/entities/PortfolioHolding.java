package com.example.portfoliomicroservice.entities;

import com.example.portfoliomicroservice.enums.HoldingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_holding", uniqueConstraints = {
        @UniqueConstraint(name = "uk_holding_account_product", columnNames = {"portfolio_account_id", "product_id"})
})
public class PortfolioHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holding_id")
    private Long holdingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_account_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_holding_portfolio_account"))
    private PortfolioAccount portfolioAccount;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "average_cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal averageCost;

    @Column(name = "market_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal marketValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "holding_status", nullable = false, length = 20)
    private HoldingStatus holdingStatus;

    @Column(name = "last_valued_at", nullable = false)
    private LocalDateTime lastValuedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (quantity == null) quantity = BigDecimal.ZERO;
        if (averageCost == null) averageCost = BigDecimal.ZERO;
        if (marketValue == null) marketValue = BigDecimal.ZERO;
        if (holdingStatus == null) holdingStatus = HoldingStatus.ACTIVE;
        if (lastValuedAt == null) lastValuedAt = now;
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getHoldingId() { return holdingId; }
    public void setHoldingId(Long holdingId) { this.holdingId = holdingId; }
    public PortfolioAccount getPortfolioAccount() { return portfolioAccount; }
    public void setPortfolioAccount(PortfolioAccount portfolioAccount) { this.portfolioAccount = portfolioAccount; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getAverageCost() { return averageCost; }
    public void setAverageCost(BigDecimal averageCost) { this.averageCost = averageCost; }
    public BigDecimal getMarketValue() { return marketValue; }
    public void setMarketValue(BigDecimal marketValue) { this.marketValue = marketValue; }
    public HoldingStatus getHoldingStatus() { return holdingStatus; }
    public void setHoldingStatus(HoldingStatus holdingStatus) { this.holdingStatus = holdingStatus; }
    public LocalDateTime getLastValuedAt() { return lastValuedAt; }
    public void setLastValuedAt(LocalDateTime lastValuedAt) { this.lastValuedAt = lastValuedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
