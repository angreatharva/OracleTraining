package com.example.tradingmicroservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "portfolio_statement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statement_id")
    private Long statementId;

    @Column(name = "portfolio_account_id")
    private Long portfolioAccountId;

    @Column(name = "holding_id")
    private Long holdingId;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "statement_start")
    private LocalDate statementStart;

    @Column(name = "statement_end")
    private LocalDate statementEnd;

    @Column(name = "opening_value", precision = 18, scale = 2)
    private BigDecimal openingValue;

    @Column(name = "closing_value", precision = 18, scale = 2)
    private BigDecimal closingValue;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "status")
    private String status;

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
