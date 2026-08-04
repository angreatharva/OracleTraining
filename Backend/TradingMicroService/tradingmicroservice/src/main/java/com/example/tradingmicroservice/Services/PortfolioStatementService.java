package com.example.tradingmicroservice.services;

import com.example.tradingmicroservice.dto.request.CreatePortfolioStatementRequest;
import com.example.tradingmicroservice.dto.response.PortfolioStatementResponse;
import com.example.tradingmicroservice.entities.PortfolioStatement;
import com.example.tradingmicroservice.entities.TradeTransaction;
import com.example.tradingmicroservice.exceptions.PortfolioStatementNotFoundException;
import com.example.tradingmicroservice.exceptions.TradeTransactionNotFoundException;
import com.example.tradingmicroservice.repositories.PortfolioStatementRepository;
import com.example.tradingmicroservice.repositories.TradeTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
@Transactional
public class PortfolioStatementService implements IPortfolioStatementService {

    private final PortfolioStatementRepository statementRepository;
    private final TradeTransactionRepository transactionRepository;

    public PortfolioStatementService(PortfolioStatementRepository statementRepository,
                                     TradeTransactionRepository transactionRepository) {
        this.statementRepository = statementRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public PortfolioStatementResponse create(CreatePortfolioStatementRequest request) {
        if (request.statementStart().isAfter(request.statementEnd())) {
            throw new IllegalArgumentException("statementStart must be before or equal to statementEnd");
        }

        List<TradeTransaction> transactions = getTransactions(request.transactionId(), request.transactionIds());
        LocalDateTime now = LocalDateTime.now();
        PortfolioStatement statement = PortfolioStatement.builder()
                .portfolioAccountId(request.portfolioAccountId())
                .holdingId(request.holdingId())
                .transactionId(request.transactionId())
                .statementStart(request.statementStart())
                .statementEnd(request.statementEnd())
                .openingValue(request.openingValue())
                .closingValue(request.closingValue())
                .generatedAt(now)
                .status("GENERATED")
                .createdAt(now)
                .updatedAt(now)
                .transactions(transactions)
                .build();

        return toResponse(statementRepository.save(statement));
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioStatementResponse getById(Long id) {
        return toResponse(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioStatementResponse> getAll(Long portfolioAccountId, String status,
                                                    LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }

        Stream<PortfolioStatement> statements = portfolioAccountId == null
                ? statementRepository.findAll().stream()
                : statementRepository.findByPortfolioAccountId(portfolioAccountId).stream();

        return statements
                .filter(statement -> status == null || statement.getStatus().equalsIgnoreCase(status))
                .filter(statement -> startDate == null || !statement.getStatementStart().isBefore(startDate))
                .filter(statement -> endDate == null || !statement.getStatementEnd().isAfter(endDate))
                .map(this::toResponse)
                .toList();
    }

    private PortfolioStatement getEntityById(Long id) {
        return statementRepository.findById(id)
                .orElseThrow(() -> new PortfolioStatementNotFoundException(id));
    }

    private List<TradeTransaction> getTransactions(Long primaryTransactionId, List<Long> transactionIds) {
        Set<Long> ids = new LinkedHashSet<>();
        ids.add(primaryTransactionId);
        if (transactionIds != null) {
            transactionIds.stream().filter(id -> id != null).forEach(ids::add);
        }

        List<TradeTransaction> transactions = new ArrayList<>();
        for (Long id : ids) {
            transactions.add(transactionRepository.findById(id)
                    .orElseThrow(() -> new TradeTransactionNotFoundException(id)));
        }
        return transactions;
    }

    private PortfolioStatementResponse toResponse(PortfolioStatement statement) {
        List<Long> transactionIds = statement.getTransactions() == null ? List.of() : statement.getTransactions().stream()
                .map(TradeTransaction::getTransactionId)
                .toList();

        return new PortfolioStatementResponse(
                statement.getStatementId(),
                statement.getPortfolioAccountId(),
                statement.getHoldingId(),
                statement.getTransactionId(),
                statement.getStatementStart(),
                statement.getStatementEnd(),
                statement.getOpeningValue(),
                statement.getClosingValue(),
                statement.getGeneratedAt(),
                statement.getStatus(),
                transactionIds
        );
    }
}
