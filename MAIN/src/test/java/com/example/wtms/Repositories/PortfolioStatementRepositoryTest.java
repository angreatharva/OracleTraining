package com.example.wtms.Repositories;

import com.example.wtms.Entities.PortfolioStatement;
import com.example.wtms.Exceptions.PortfolioStatementNotFoundException;
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

    /*
     * Helper method
     * If the statement does not exist, throw a custom exception.
     */
    private PortfolioStatement getStatementOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new PortfolioStatementNotFoundException(id));
    }

    @Test
    @DisplayName("Find Portfolio Statement by ID")
    void testFindById() {

        System.out.println("==================================================");
        System.out.println("TEST : Find Portfolio Statement by ID");
        System.out.println("==================================================");

        Optional<PortfolioStatement> statement = repository.findById(1L);

        System.out.println("Searching for Statement ID : 1");

        assertTrue(statement.isPresent(), "Portfolio Statement should exist.");

        System.out.println("Statement Found Successfully");
        System.out.println("Statement Status : " + statement.get().getStatus());
        System.out.println("Opening Value    : " + statement.get().getOpeningValue());
        System.out.println("Closing Value    : " + statement.get().getClosingValue());

        assertEquals("GENERATED", statement.get().getStatus());

        System.out.println("Test Passed");
    }

    @Test
    @DisplayName("Throw Exception if Portfolio Statement is not found")
    void testPortfolioStatementNotFoundException() {

        System.out.println("==================================================");
        System.out.println("TEST : Portfolio Statement Not Found");
        System.out.println("==================================================");

        Long invalidId = 9999L;

        PortfolioStatementNotFoundException exception =
                assertThrows(
                        PortfolioStatementNotFoundException.class,
                        () -> getStatementOrThrow(invalidId)
                );

        System.out.println("Exception Successfully Thrown");
        System.out.println(exception.getMessage());

        assertEquals(
                "Portfolio Statement not found with id: " + invalidId,
                exception.getMessage()
        );

        System.out.println("Test Passed");
    }

    @Test
    @DisplayName("Find Statements by Status")
    void testFindByStatus() {

        System.out.println("==================================================");
        System.out.println("TEST : Find Statements by Status");
        System.out.println("==================================================");

        List<PortfolioStatement> statements =
                repository.findByStatus("GENERATED");

        System.out.println("Searching for Status : GENERATED");
        System.out.println("Total Statements Found : " + statements.size());

        assertFalse(statements.isEmpty());

        for (PortfolioStatement statement : statements) {

            System.out.println("---------------------------------------");
            System.out.println("Statement ID : " + statement.getStatementId());
            System.out.println("Status       : " + statement.getStatus());

            assertEquals("GENERATED", statement.getStatus());
        }

        System.out.println("Test Passed");
    }

    @Test
    @DisplayName("Find Statements by Portfolio Account")
    void testFindByPortfolioAccount() {

        System.out.println("==================================================");
        System.out.println("TEST : Find Statements by Portfolio Account");
        System.out.println("==================================================");

        List<PortfolioStatement> statements =
                repository.findByPortfolioAccount_PortfolioAccountId(1L);

        System.out.println("Searching Portfolio Account ID : 1");

        assertFalse(statements.isEmpty());

        PortfolioStatement statement = statements.get(0);

        System.out.println("Statement ID          : " + statement.getStatementId());
        System.out.println("Portfolio Account ID  : "
                + statement.getPortfolioAccount().getPortfolioAccountId());

        assertEquals(
                1L,
                statement.getPortfolioAccount().getPortfolioAccountId()
        );

        System.out.println("Test Passed");
    }

    @Test
    @DisplayName("Find Statements by Holding")
    void testFindByHolding() {

        System.out.println("==================================================");
        System.out.println("TEST : Find Statements by Holding");
        System.out.println("==================================================");

        List<PortfolioStatement> statements =
                repository.findByHolding_HoldingId(1L);

        System.out.println("Searching Holding ID : 1");

        assertFalse(statements.isEmpty());

        PortfolioStatement statement = statements.get(0);

        System.out.println("Statement ID : " + statement.getStatementId());
        System.out.println("Holding ID   : "
                + statement.getHolding().getHoldingId());

        assertEquals(
                1L,
                statement.getHolding().getHoldingId()
        );

        System.out.println("Test Passed");
    }

    @Test
    @DisplayName("Find Statements Between Dates")
    void testFindByStatementStartBetween() {

        System.out.println("==================================================");
        System.out.println("TEST : Find Statements Between Dates");
        System.out.println("==================================================");

        LocalDate startDate = LocalDate.of(2026,1,1);
        LocalDate endDate   = LocalDate.of(2026,1,31);

        List<PortfolioStatement> statements =
                repository.findByStatementStartBetween(startDate, endDate);

        System.out.println("Searching Between");
        System.out.println("Start Date : " + startDate);
        System.out.println("End Date   : " + endDate);

        System.out.println("Total Statements Found : "
                + statements.size());

        assertFalse(statements.isEmpty());

        for (PortfolioStatement statement : statements) {

            System.out.println("--------------------------------");
            System.out.println("Statement ID : "
                    + statement.getStatementId());
            System.out.println("Start Date   : "
                    + statement.getStatementStart());
            System.out.println("End Date     : "
                    + statement.getStatementEnd());
        }

        System.out.println("Test Passed");
    }
}