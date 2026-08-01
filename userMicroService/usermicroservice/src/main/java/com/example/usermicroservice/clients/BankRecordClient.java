package com.example.usermicroservice.clients;

import com.example.usermicroservice.exceptions.UserDeletionBlockedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Reads Bank-owned records before a User is deleted. The BANK-SERVICE host name
 * is resolved through Eureka and Spring Cloud LoadBalancer.
 */
@Component
public class BankRecordClient {

    private final RestClient restClient;

    public BankRecordClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("http://BANK-SERVICE").build();
    }

    public boolean hasBankAccountOrKycDocument(Long userId) {
        try {
            BankAccountReference[] accounts = restClient.get()
                    .uri("/api/bank-accounts?userId={userId}", userId)
                    .retrieve()
                    .body(BankAccountReference[].class);
            if (accounts != null && accounts.length > 0) {
                return true;
            }

            KycDocumentReference[] documents = restClient.get()
                    .uri("/api/kyc-documents?userId={userId}", userId)
                    .retrieve()
                    .body(KycDocumentReference[].class);
            return documents != null && documents.length > 0;
        } catch (RestClientException exception) {
            // Fail closed: a user must not be deleted if Bank cannot be checked.
            throw new UserDeletionBlockedException(userId, "Bank Service could not verify the user's records", exception);
        }
    }

    private record BankAccountReference(Long bankAccountId) {
    }

    private record KycDocumentReference(Long kycDocumentId) {
    }
}
