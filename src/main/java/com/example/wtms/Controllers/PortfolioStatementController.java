package com.example.wtms.Controllers;

import com.example.wtms.Entities.PortfolioStatement;
import com.example.wtms.Services.IPortfolioStatementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/portfolio-statements")
public class PortfolioStatementController {

    private final IPortfolioStatementService statementService;

    public PortfolioStatementController(IPortfolioStatementService statementService) {
        this.statementService = statementService;
    }

    @PostMapping
    public ResponseEntity<PortfolioStatement> create(@RequestBody PortfolioStatement statement) {
        return ResponseEntity.status(HttpStatus.CREATED).body(statementService.create(statement));
    }

    @GetMapping
    public List<PortfolioStatement> getAll() {
        return statementService.getAll();
    }

    @GetMapping("/{id}")
    public PortfolioStatement getById(@PathVariable Long id) {
        return statementService.getById(id);
    }

    @GetMapping("/status/{status}")
    public List<PortfolioStatement> getByStatus(@PathVariable String status) {
        return statementService.getByStatus(status);
    }

    @GetMapping("/portfolio-account/{portfolioAccountId}")
    public List<PortfolioStatement> getByPortfolioAccountId(@PathVariable Long portfolioAccountId) {
        return statementService.getByPortfolioAccountId(portfolioAccountId);
    }

    @GetMapping("/holding/{holdingId}")
    public List<PortfolioStatement> getByHoldingId(@PathVariable Long holdingId) {
        return statementService.getByHoldingId(holdingId);
    }

    @GetMapping("/start-date")
    public List<PortfolioStatement> getByStatementStartBetween(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return statementService.getByStatementStartBetween(startDate, endDate);
    }

    @PutMapping("/{id}")
    public PortfolioStatement update(@PathVariable Long id, @RequestBody PortfolioStatement statement) {
        return statementService.update(id, statement);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        statementService.delete(id);
    }
}
