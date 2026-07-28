package com.example.tradingmicroservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;


@Configuration
public class RestClientConfiguration {

    @Bean
    @Primary
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3_000);
        // A trade invokes multiple services and each one may commit a database update.
        // Do not time out a valid Portfolio response before its database transaction finishes.
        requestFactory.setReadTimeout(30_000);

        return RestClient.builder().requestFactory(requestFactory);
    }

    @Bean
    @Qualifier("portfolioRestClientBuilder")
    RestClient.Builder portfolioRestClientBuilder() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3_000);
        requestFactory.setReadTimeout(30_000);
        return RestClient.builder().requestFactory(requestFactory);
    }
}
