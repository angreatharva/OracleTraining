package com.example.bankmicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BankmicroserviceApplication {

    public static void main(String[] args) {

        SpringApplication.run(BankmicroserviceApplication.class, args);
        System.out.println("BANK - MICROSERVICE STARTED...");
    }

}
