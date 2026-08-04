package com.example.apigateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @GetMapping("/userFallback")
    public String userServiceFallback() {
        return "User service is taking longer than expected\nPlease try after sometime";
    }

    @GetMapping("/bankFallback")
    public String bankServiceFallback() {
        return "Bank service is taking longer than expected\nPlease try after sometime";
    }

    @GetMapping("/portfolioFallback")
    public String portfolioServiceFallback() {
        return "Portfolio service is taking longer than expected\nPlease try after sometime";
    }

    @GetMapping("/tradingFallback")
    public String tradingServiceFallback() {
        return "Trading service is taking longer than expected\nPlease try after sometime";
    }

    @GetMapping("/productFallback")
    public String productServiceFallback() {
        return "Product service is taking longer than expected\nPlease try after sometime";
    }
}