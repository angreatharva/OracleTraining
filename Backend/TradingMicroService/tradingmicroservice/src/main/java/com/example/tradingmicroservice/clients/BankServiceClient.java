package com.example.tradingmicroservice.clients;

import com.example.tradingmicroservice.clients.model.DebitRequest;
import com.example.tradingmicroservice.clients.model.DebitResult;
import com.example.tradingmicroservice.clients.model.CreditRequest;
import com.example.tradingmicroservice.clients.model.CreditResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Bank API contract resolved through Eureka by service name.
 */
@FeignClient(name = "BANK-SERVICE")
public interface BankServiceClient {

    @PostMapping("/api/bank-accounts/{bankAccountId}/debit")
    DebitResult authorizeDebit(@PathVariable("bankAccountId") Long bankAccountId,
                               @RequestBody DebitRequest request);

    @PostMapping("/api/bank-accounts/{bankAccountId}/credit")
    CreditResult credit(@PathVariable("bankAccountId") Long bankAccountId,
                        @RequestBody CreditRequest request);
}
