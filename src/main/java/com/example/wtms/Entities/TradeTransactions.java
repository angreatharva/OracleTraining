package com.example.wtms.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_account_id", nullable = false)
    private PortfolioAccount portfolioAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holding_id", nullable = false)
    private Holding holding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "quantity", precision = 18, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit_price", precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "transaction_status")
    private String transactionStatus;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "statement_transaction",
            joinColumns = @JoinColumn(name = "statement_id"),
            inverseJoinColumns = @JoinColumn(name = "transaction_id")
    )
    private List<TradeTransaction> transactions;
}