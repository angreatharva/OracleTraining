package com.example.portfoliomicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class PortfoliomicroserviceApplication {

    public static void main(String[] args) {

        SpringApplication.run(PortfoliomicroserviceApplication.class, args);
        System.out.println("PORTFOLIO - MICROSERVICE STARTED...");

    }

}
