package com.example.tradingmicroservice.services;

import com.example.tradingmicroservice.dto.request.CreateTradeTransactionRequest;
import com.example.tradingmicroservice.dto.response.TradeTransactionResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface ITradeTransactionService {

    TradeTransactionResponse create(CreateTradeTransactionRequest request);

    TradeTransactionResponse getById(Long id);

    List<TradeTransactionResponse> getAll(Long portfolioAccountId, String status, String type,
                                          LocalDateTime startDate, LocalDateTime endDate);
}
