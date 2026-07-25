package com.example.wtms.Controllers;

import com.example.wtms.Entities.TradeTransaction;
import com.example.wtms.Services.ITradeTransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trade-transactions")
public class TradeTransactionController {

    private final ITradeTransactionService transactionService;

    public TradeTransactionController(ITradeTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TradeTransaction> create(@RequestBody TradeTransaction transaction) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(transaction));
    }

    @GetMapping
    public List<TradeTransaction> getAll() {
        return transactionService.getAll();
    }

    @GetMapping("/{id}")
    public TradeTransaction getById(@PathVariable Long id) {
        return transactionService.getById(id);
    }

    @GetMapping("/status/{status}")
    public List<TradeTransaction> getByStatus(@PathVariable String status) {
        return transactionService.getByStatus(status);
    }

    @GetMapping("/type/{type}")
    public List<TradeTransaction> getByType(@PathVariable String type) {
        return transactionService.getByType(type);
    }

    @GetMapping("/portfolio-account/{portfolioAccountId}")
    public List<TradeTransaction> getByPortfolioAccountId(@PathVariable Long portfolioAccountId) {
        return transactionService.getByPortfolioAccountId(portfolioAccountId);
    }

    @GetMapping("/holding/{holdingId}")
    public List<TradeTransaction> getByHoldingId(@PathVariable Long holdingId) {
        return transactionService.getByHoldingId(holdingId);
    }

    @GetMapping("/product/{productId}")
    public List<TradeTransaction> getByProductId(@PathVariable Long productId) {
        return transactionService.getByProductId(productId);
    }

    @PutMapping("/{id}")
    public TradeTransaction update(@PathVariable Long id, @RequestBody TradeTransaction transaction) {
        return transactionService.update(id, transaction);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        transactionService.delete(id);
    }
}
