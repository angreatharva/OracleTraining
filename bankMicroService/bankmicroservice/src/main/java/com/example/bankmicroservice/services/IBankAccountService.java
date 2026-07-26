package com.example.bankmicroservice.services;

import com.example.bankmicroservice.dto.request.CreateBankAccountRequest;
import com.example.bankmicroservice.dto.request.CreditRequest;
import com.example.bankmicroservice.dto.request.DebitRequest;
import com.example.bankmicroservice.dto.request.UpdateBankAccountRequest;
import com.example.bankmicroservice.dto.response.BankAccountResponse;
import com.example.bankmicroservice.dto.response.CreditResult;
import com.example.bankmicroservice.dto.response.DebitResult;

import java.util.List;

public interface IBankAccountService {

    BankAccountResponse create(CreateBankAccountRequest request);

    BankAccountResponse getById(Long id);

    List<BankAccountResponse> getAll(Long userId, String status, Boolean primary);

    BankAccountResponse update(Long id, UpdateBankAccountRequest request);

    BankAccountResponse makePrimary(Long id);

    DebitResult debit(Long id, DebitRequest request);

    CreditResult credit(Long id, CreditRequest request);

    void close(Long id);
}
