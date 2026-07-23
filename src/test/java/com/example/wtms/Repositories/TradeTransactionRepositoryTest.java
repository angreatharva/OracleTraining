package com.example.wtms.Repositories;

import com.example.wtms.Entities.TradeTransaction;
import com.example.wtms.Exceptions.TradeTransactionNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TradeTransactionRepositoryTest {

    @Autowired
    private TradeTransactionRepository repository;

    // Helper method used only for testing exception flow
    private TradeTransaction getTransactionOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new TradeTransactionNotFoundException(id));
    }

    @Test
    @DisplayName("Find transaction by ID")
    void testFindById() {
        System.out.println("---- Test: Find transaction by ID ----");

        Optional<TradeTransaction> transaction = repository.findById(1L);

        System.out.println("Searching for transaction with ID = 1");

        assertTrue(transaction.isPresent(), "Transaction should be present");

        System.out.println("Transaction found");
        System.out.println("Transaction Type: " + transaction.get().getTransactionType());
        System.out.println("Transaction Status: " + transaction.get().getTransactionStatus());

        assertEquals("BUY", transaction.get().getTransactionType());
        assertEquals("COMPLETED", transaction.get().getTransactionStatus());
    }

    @Test
    @DisplayName("Throw exception when transaction is not found")
    void testTransactionNotFoundException() {
        System.out.println("---- Test: Transaction not found exception ----");

        Long invalidId = 9999L;

        TradeTransactionNotFoundException exception = assertThrows(
                TradeTransactionNotFoundException.class,
                () -> getTransactionOrThrow(invalidId)
        );

        System.out.println("Exception thrown successfully");
        System.out.println("Message: " + exception.getMessage());

        assertEquals("Trade Transaction not found with id: " + invalidId, exception.getMessage());
    }

    @Test
    @DisplayName("Find transactions by transaction status")
    void testFindByTransactionStatus() {
        System.out.println("---- Test: Find by transaction status ----");

        List<TradeTransaction> transactions = repository.findByTransactionStatus("COMPLETED");

        System.out.println("Searching for transactions with status = COMPLETED");
        System.out.println("Number of transactions found: " + transactions.size());

        assertFalse(transactions.isEmpty(), "Transaction list should not be empty");

        for (TradeTransaction t : transactions) {
            System.out.println(
                    "Transaction ID: " + t.getTransactionId() +
                            ", Type: " + t.getTransactionType() +
                            ", Status: " + t.getTransactionStatus()
            );
            assertEquals("COMPLETED", t.getTransactionStatus());
        }
    }

    @Test
    @DisplayName("Find transactions by transaction type")
    void testFindByTransactionType() {
        System.out.println("---- Test: Find by transaction type ----");

        List<TradeTransaction> transactions = repository.findByTransactionType("BUY");

        System.out.println("Searching for transactions with type = BUY");
        System.out.println("Number of transactions found: " + transactions.size());

        assertFalse(transactions.isEmpty(), "Transaction list should not be empty");

        for (TradeTransaction t : transactions) {
            System.out.println(
                    "Transaction ID: " + t.getTransactionId() +
                            ", Type: " + t.getTransactionType()
            );
            assertEquals("BUY", t.getTransactionType());
        }
    }

    @Test
    @DisplayName("Find transactions by portfolio account ID")
    void testFindByPortfolioAccount() {
        System.out.println("---- Test: Find by portfolio account ----");

        List<TradeTransaction> transactions =
                repository.findByPortfolioAccount_PortfolioAccountId(1L);

        System.out.println("Searching for transactions for portfolio account ID = 1");
        System.out.println("Number of transactions found: " + transactions.size());

        assertFalse(transactions.isEmpty(), "Transaction list should not be empty");

        TradeTransaction firstTransaction = transactions.get(0);

        System.out.println("First Transaction ID: " + firstTransaction.getTransactionId());
        System.out.println("Portfolio Account ID: " +
                firstTransaction.getPortfolioAccount().getPortfolioAccountId());

        assertEquals(1L, firstTransaction.getPortfolioAccount().getPortfolioAccountId());
    }

    @Test
    @DisplayName("Find transactions by holding ID")
    void testFindByHolding() {
        System.out.println("---- Test: Find by holding ----");

        List<TradeTransaction> transactions = repository.findByHolding_HoldingId(1L);

        System.out.println("Searching for transactions for holding ID = 1");
        System.out.println("Number of transactions found: " + transactions.size());

        assertFalse(transactions.isEmpty(), "Transaction list should not be empty");

        TradeTransaction firstTransaction = transactions.get(0);

        System.out.println("First Transaction ID: " + firstTransaction.getTransactionId());
        System.out.println("Holding ID: " + firstTransaction.getHolding().getHoldingId());

        assertEquals(1L, firstTransaction.getHolding().getHoldingId());
    }

    @Test
    @DisplayName("Find transactions by product ID")
    void testFindByProduct() {
        System.out.println("---- Test: Find by product ----");

        List<TradeTransaction> transactions = repository.findByProduct_ProductId(1L);

        System.out.println("Searching for transactions for product ID = 1");
        System.out.println("Number of transactions found: " + transactions.size());

        assertFalse(transactions.isEmpty(), "Transaction list should not be empty");

        TradeTransaction firstTransaction = transactions.get(0);

        System.out.println("First Transaction ID: " + firstTransaction.getTransactionId());
        System.out.println("Product ID: " + firstTransaction.getProduct().getProductId());

        assertEquals(1L, firstTransaction.getProduct().getProductId());
    }
}
