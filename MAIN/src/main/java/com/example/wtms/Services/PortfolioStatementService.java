package com.example.wtms.Services;

import com.example.wtms.Entities.PortfolioStatement;
import com.example.wtms.Exceptions.PortfolioStatementNotFoundException;
import com.example.wtms.Repositories.PortfolioStatementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PortfolioStatementService implements IPortfolioStatementService {

    private final PortfolioStatementRepository statementRepository;

    public PortfolioStatementService(PortfolioStatementRepository statementRepository) {
        this.statementRepository = statementRepository;
    }

    @Override
    public PortfolioStatement create(PortfolioStatement statement) {
        statement.setStatementId(null);
        LocalDateTime now = LocalDateTime.now();
        statement.setCreatedAt(now);
        statement.setUpdatedAt(now);
        return statementRepository.save(statement);
    }

    @Override
    public PortfolioStatement getById(Long id) {
        return statementRepository.findById(id)
                .orElseThrow(() -> new PortfolioStatementNotFoundException(id));
    }

    @Override
    public List<PortfolioStatement> getAll() {
        return statementRepository.findAll();
    }

    @Override
    public List<PortfolioStatement> getByStatus(String status) {
        return statementRepository.findByStatus(status);
    }

    @Override
    public List<PortfolioStatement> getByPortfolioAccountId(Long portfolioAccountId) {
        return statementRepository.findByPortfolioAccount_PortfolioAccountId(portfolioAccountId);
    }

    @Override
    public List<PortfolioStatement> getByHoldingId(Long holdingId) {
        return statementRepository.findByHolding_HoldingId(holdingId);
    }

    @Override
    public List<PortfolioStatement> getByStatementStartBetween(LocalDate startDate, LocalDate endDate) {
        return statementRepository.findByStatementStartBetween(startDate, endDate);
    }

    @Override
    public PortfolioStatement update(Long id, PortfolioStatement statement) {
        PortfolioStatement existing = getById(id);
        statement.setStatementId(existing.getStatementId());
        statement.setCreatedAt(existing.getCreatedAt());
        statement.setUpdatedAt(LocalDateTime.now());
        return statementRepository.save(statement);
    }

    @Override
    public void delete(Long id) {
        statementRepository.delete(getById(id));
    }
}
