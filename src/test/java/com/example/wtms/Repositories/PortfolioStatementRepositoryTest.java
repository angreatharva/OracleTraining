package com.example.wtms.Repositories;

import com.example.wtms.Entities.PortfolioStatement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PortfolioStatementRepositoryTest {

    @Autowired
    private PortfolioStatementRepository repository;

    @Test
    @DisplayName("Find portfolio statement by ID")
    void testFindById() {
        System.out.println("---- Test: Find statement by ID ----");

        Optional<PortfolioStatement> statement = repository.findById(1L);

        System.out.println("Searching for statement with ID = 1");

        assertTrue(statement.isPresent(), "Statement should be present");

        System.out.println("Statement found");
        System.out.println("Statement Status: " + statement.get().getStatus());
        System.out.println("Statement Start: " + statement.get().getStatementStart());
        System.out.println("Statement End: " + statement.get().getStatementEnd());

        assertEquals("GENERATED", statement.get().getStatus());
    }

    @Test
    @DisplayName("Find portfolio statements by status")
    void testFindByStatus() {
        System.out.println("---- Test: Find by status ----");

        List<PortfolioStatement> statements = repository.findByStatus("GENERATED");

        System.out.println("Searching for statements with status = GENERATED");
        System.out.println("Number of statements found: " + statements.size());

        assertFalse(statements.isEmpty(), "Statement list should not be empty");

        for (PortfolioStatement s : statements) {
            System.out.println(
                    "Statement ID: " + s.getStatementId() +
                            ", Status: " + s.getStatus()
            );
            assertEquals("GENERATED", s.getStatus());
        }
    }

    @Test
    @DisplayName("Find portfolio statements by portfolio account ID")
    void testFindByPortfolioAccount() {
        System.out.println("---- Test: Find by portfolio account ----");

        List<PortfolioStatement> statements =
                repository.findByPortfolioAccount_PortfolioAccountId(1L);

        System.out.println("Searching for statements for portfolio account ID = 1");
        System.out.println("Number of statements found: " + statements.size());

        assertFalse(statements.isEmpty(), "Statement list should not be empty");

        PortfolioStatement firstStatement = statements.get(0);

        System.out.println("First Statement ID: " + firstStatement.getStatementId());
        System.out.println("Portfolio Account ID: " +
                firstStatement.getPortfolioAccount().getPortfolioAccountId());

        assertEquals(1L, firstStatement.getPortfolioAccount().getPortfolioAccountId());
    }

    @Test
    @DisplayName("Find portfolio statements by holding ID")
    void testFindByHolding() {
        System.out.println("---- Test: Find by holding ----");

        List<PortfolioStatement> statements =
                repository.findByHolding_HoldingId(1L);

        System.out.println("Searching for statements for holding ID = 1");
        System.out.println("Number of statements found: " + statements.size());

        assertFalse(statements.isEmpty(), "Statement list should not be empty");

        PortfolioStatement firstStatement = statements.get(0);

        System.out.println("First Statement ID: " + firstStatement.getStatementId());
        System.out.println("Holding ID: " + firstStatement.getHolding().getHoldingId());

        assertEquals(1L, firstStatement.getHolding().getHoldingId());
    }

    @Test
    @DisplayName("Find portfolio statements by statement start date range")
    void testFindByStatementStartBetween() {
        System.out.println("---- Test: Find by statement start date range ----");

        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);

        List<PortfolioStatement> statements =
                repository.findByStatementStartBetween(startDate, endDate);

        System.out.println("Searching for statements between " + startDate + " and " + endDate);
        System.out.println("Number of statements found: " + statements.size());

        assertFalse(statements.isEmpty(), "Statement list should not be empty");

        for (PortfolioStatement s : statements) {
            System.out.println(
                    "Statement ID: " + s.getStatementId() +
                            ", Start Date: " + s.getStatementStart() +
                            ", End Date: " + s.getStatementEnd()
            );
        }
    }
}