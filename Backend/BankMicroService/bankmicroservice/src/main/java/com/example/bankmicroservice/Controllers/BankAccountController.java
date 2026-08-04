package com.example.bankmicroservice.controllers;

import com.example.bankmicroservice.dto.request.CreateBankAccountRequest;
import com.example.bankmicroservice.dto.request.CreditRequest;
import com.example.bankmicroservice.dto.request.DebitRequest;
import com.example.bankmicroservice.dto.request.UpdateBankAccountRequest;
import com.example.bankmicroservice.dto.response.BankAccountResponse;
import com.example.bankmicroservice.dto.response.CreditResult;
import com.example.bankmicroservice.dto.response.DebitResult;
import com.example.bankmicroservice.services.IBankAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bank-accounts")
public class BankAccountController implements IBankAccountController {

    private final IBankAccountService bankAccountService;

    public BankAccountController(IBankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @PostMapping
    @Override
    public ResponseEntity<BankAccountResponse> create(
            @Valid @RequestBody CreateBankAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bankAccountService.create(request));
    }

    @GetMapping("/{id}")
    @Override
    public BankAccountResponse getById(@PathVariable Long id) {
        return bankAccountService.getById(id);
    }

    @GetMapping
    @Override
    public List<BankAccountResponse> getAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean primary) {
        return bankAccountService.getAll(userId, status, primary);
    }

    @PutMapping("/{id}")
    @Override
    public BankAccountResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBankAccountRequest request) {
        return bankAccountService.update(id, request);
    }

    @PatchMapping("/{id}/primary")
    @Override
    public BankAccountResponse makePrimary(@PathVariable Long id) {
        return bankAccountService.makePrimary(id);
    }

    @PostMapping("/{id}/debit")
    @Override
    public DebitResult debit(@PathVariable Long id, @Valid @RequestBody DebitRequest request) {
        return bankAccountService.debit(id, request);
    }

    @PostMapping("/{id}/credit")
    @Override
    public CreditResult credit(@PathVariable Long id, @Valid @RequestBody CreditRequest request) {
        return bankAccountService.credit(id, request);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> close(@PathVariable Long id) {
        bankAccountService.close(id);
        return ResponseEntity.noContent().build();
    }
}
