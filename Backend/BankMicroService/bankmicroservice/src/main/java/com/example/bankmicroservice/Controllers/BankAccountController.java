package com.example.bankmicroservice.controllers;

import com.example.bankmicroservice.dto.request.CreateBankAccountRequest;
import com.example.bankmicroservice.dto.request.CreditRequest;
import com.example.bankmicroservice.dto.request.DebitRequest;
import com.example.bankmicroservice.dto.request.UpdateBankAccountRequest;
import com.example.bankmicroservice.dto.response.BankAccountResponse;
import com.example.bankmicroservice.dto.response.CreditResult;
import com.example.bankmicroservice.dto.response.DebitResult;
import com.example.bankmicroservice.security.AuthorizationHelper;
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

/**
 * Authorization notes:
 * <ul>
 *   <li>reads and single-account actions are checked against {@code bank_account.user_id};</li>
 *   <li>editing account metadata/status is MANAGER-only (it is how an account gets blocked
 *       or closed administratively);</li>
 *   <li>debit and credit are SERVICE-only - money movement belongs to the Trading saga, and
 *       exposing it to an end-user token would let anyone move their own balance at will.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/bank-accounts")
public class BankAccountController implements IBankAccountController {

    private final IBankAccountService bankAccountService;
    private final AuthorizationHelper authorization;

    public BankAccountController(IBankAccountService bankAccountService, AuthorizationHelper authorization) {
        this.bankAccountService = bankAccountService;
        this.authorization = authorization;
    }

    @PostMapping
    @Override
    public ResponseEntity<BankAccountResponse> create(
            @Valid @RequestBody CreateBankAccountRequest request) {
        authorization.assertCanAccessUser(request.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(bankAccountService.create(request));
    }

    @GetMapping("/{id}")
    @Override
    public BankAccountResponse getById(@PathVariable Long id) {
        BankAccountResponse account = bankAccountService.getById(id);
        authorization.assertCanAccessUser(account.userId());
        return account;
    }

    @GetMapping
    @Override
    public List<BankAccountResponse> getAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean primary) {
        // Narrowed to the caller: an investor cannot list the whole table by omitting userId.
        return bankAccountService.getAll(authorization.restrictUserFilter(userId), status, primary);
    }

    @PutMapping("/{id}")
    @Override
    public BankAccountResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBankAccountRequest request) {
        authorization.assertManager();
        return bankAccountService.update(id, request);
    }

    @PatchMapping("/{id}/primary")
    @Override
    public BankAccountResponse makePrimary(@PathVariable Long id) {
        authorization.assertCanAccessUser(bankAccountService.getById(id).userId());
        return bankAccountService.makePrimary(id);
    }

    @PostMapping("/{id}/debit")
    @Override
    public DebitResult debit(@PathVariable Long id, @Valid @RequestBody DebitRequest request) {
        authorization.assertServiceCall();
        return bankAccountService.debit(id, request);
    }

    @PostMapping("/{id}/credit")
    @Override
    public CreditResult credit(@PathVariable Long id, @Valid @RequestBody CreditRequest request) {
        authorization.assertServiceCall();
        return bankAccountService.credit(id, request);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> close(@PathVariable Long id) {
        authorization.assertManager();
        bankAccountService.close(id);
        return ResponseEntity.noContent().build();
    }
}
