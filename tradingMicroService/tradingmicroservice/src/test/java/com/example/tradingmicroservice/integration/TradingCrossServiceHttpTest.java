package com.example.tradingmicroservice.integration;

import com.example.tradingmicroservice.clients.BankServiceClient;
import com.example.tradingmicroservice.clients.PortfolioServiceClient;
import com.example.tradingmicroservice.clients.ProductServiceClient;
import com.example.tradingmicroservice.clients.model.DebitRequest;
import com.example.tradingmicroservice.clients.model.HoldingUpdateRequest;
import com.example.tradingmicroservice.clients.model.ProductQuote;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Executes the real Feign clients against a local HTTP server and verifies the
 * method, path and JSON contracts shared with Product, Bank and Portfolio.
 */
@SpringBootTest(
        classes = TradingCrossServiceHttpTest.FeignContractTestConfiguration.class,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false"
        }
)
class TradingCrossServiceHttpTest {

    private static final List<String> REQUESTS = new ArrayList<>();
    private static final HttpServer SERVER = startServer();

    @Autowired
    private ProductServiceClient productClient;

    @Autowired
    private BankServiceClient bankClient;

    @Autowired
    private PortfolioServiceClient portfolioClient;

    @DynamicPropertySource
    static void clientUrls(DynamicPropertyRegistry registry) {
        String url = "http://localhost:" + SERVER.getAddress().getPort();
        registry.add("spring.cloud.openfeign.client.config.PRODUCT-SERVICE.url", () -> url);
        registry.add("spring.cloud.openfeign.client.config.BANK-SERVICE.url", () -> url);
        registry.add("spring.cloud.openfeign.client.config.PORTFOLIO-SERVICE.url", () -> url);
    }

    @AfterAll
    static void stopServer() {
        SERVER.stop(0);
    }

    @Test
    void feignClientsUseThePublishedCrossServiceContracts() {
        ProductQuote product = productClient.getActiveProduct(1L);
        HoldingUpdateRequest trade = new HoldingUpdateRequest(
                1L, 9L, 1L, "BUY", new BigDecimal("2.00"),
                product.currentPrice(), 700L);

        portfolioClient.validateTrade(trade);
        assertTrue(bankClient.authorizeDebit(
                11L, new DebitRequest(new BigDecimal("301.00"), "TRADE-700")).approved());
        portfolioClient.applyCompletedTrade(trade);

        assertEquals(new BigDecimal("150.50"), product.currentPrice());
        assertEquals(List.of(
                "GET /api/products/1 ",
                "POST /api/portfolios/internal/trades/validate "
                        + "{\"portfolioAccountId\":1,\"holdingId\":9,\"productId\":1,"
                        + "\"transactionType\":\"BUY\",\"quantity\":2.00,\"unitPrice\":150.50,"
                        + "\"transactionId\":700}",
                "POST /api/bank-accounts/11/debit "
                        + "{\"amount\":301.00,\"transactionReference\":\"TRADE-700\"}",
                "POST /api/portfolios/internal/trades "
                        + "{\"portfolioAccountId\":1,\"holdingId\":9,\"productId\":1,"
                        + "\"transactionType\":\"BUY\",\"quantity\":2.00,\"unitPrice\":150.50,"
                        + "\"transactionId\":700}"
        ), REQUESTS);
    }

    private static HttpServer startServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/", TradingCrossServiceHttpTest::handle);
            server.start();
            return server;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not start contract-test HTTP server", exception);
        }
    }

    private static void handle(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        REQUESTS.add(exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath() + " " + body);

        String path = exchange.getRequestURI().getPath();
        if ("/api/products/1".equals(path)) {
            respond(exchange, 200,
                    "{\"productId\":1,\"productName\":\"Demo Stock\","
                            + "\"currentPrice\":150.50,\"active\":true}");
        } else if ("/api/bank-accounts/11/debit".equals(path)) {
            respond(exchange, 200,
                    "{\"approved\":true,\"reference\":\"TRADE-700\",\"failureReason\":null}");
        } else {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        }
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class
    })
    @EnableFeignClients(clients = {
            ProductServiceClient.class,
            BankServiceClient.class,
            PortfolioServiceClient.class
    })
    static class FeignContractTestConfiguration {
    }
}
