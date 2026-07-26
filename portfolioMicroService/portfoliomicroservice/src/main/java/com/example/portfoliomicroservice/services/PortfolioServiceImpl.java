package com.example.portfoliomicroservice.services;

import com.example.portfoliomicroservice.clients.ProductServiceClient;
import com.example.portfoliomicroservice.clients.UserServiceClient;
import com.example.portfoliomicroservice.domain.PortfolioMath;
import com.example.portfoliomicroservice.dto.request.CreateHoldingRequest;
import com.example.portfoliomicroservice.dto.request.CreatePortfolioAccountRequest;
import com.example.portfoliomicroservice.dto.request.UpdateHoldingRequest;
import com.example.portfoliomicroservice.dto.request.UpdatePortfolioStatusRequest;
import com.example.portfoliomicroservice.dto.response.PortfolioAccountResponse;
import com.example.portfoliomicroservice.dto.response.PortfolioHoldingResponse;
import com.example.portfoliomicroservice.dto.response.PortfolioSummaryResponse;
import com.example.portfoliomicroservice.entities.PortfolioAccount;
import com.example.portfoliomicroservice.entities.PortfolioHolding;
import com.example.portfoliomicroservice.enums.AccountStatus;
import com.example.portfoliomicroservice.enums.HoldingStatus;
import com.example.portfoliomicroservice.exceptions.BusinessRuleException;
import com.example.portfoliomicroservice.exceptions.ResourceNotFoundException;
import com.example.portfoliomicroservice.repositories.PortfolioAccountRepository;
import com.example.portfoliomicroservice.repositories.PortfolioHoldingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PortfolioServiceImpl implements PortfolioService {

    private static final int MONEY_SCALE = 4;

    private final PortfolioAccountRepository accountRepository;
    private final PortfolioHoldingRepository holdingRepository;
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;

    public PortfolioServiceImpl(PortfolioAccountRepository accountRepository,
                                PortfolioHoldingRepository holdingRepository,
                                UserServiceClient userServiceClient,
                                ProductServiceClient productServiceClient) {
        this.accountRepository = accountRepository;
        this.holdingRepository = holdingRepository;
        this.userServiceClient = userServiceClient;
        this.productServiceClient = productServiceClient;
    }

    @Override
    public PortfolioAccountResponse createAccount(CreatePortfolioAccountRequest request) {
        userServiceClient.validateUser(request.userId());

        if (accountRepository.existsByUserId(request.userId())) {
            throw new BusinessRuleException("A portfolio account already exists for user " + request.userId());
        }

        LocalDate openedDate = request.openedDate() == null ? LocalDate.now() : request.openedDate();
        if (openedDate.isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Opened date cannot be in the future");
        }

        PortfolioAccount account = new PortfolioAccount();
        account.setUserId(request.userId());
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setOpenedDate(openedDate);
        return mapAccount(accountRepository.save(account));
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioAccountResponse getAccount(Long portfolioAccountId) {
        return mapAccount(findAccount(portfolioAccountId));
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioAccountResponse getAccountByUser(Long userId) {
        return mapAccount(accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio account not found for user " + userId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioAccountResponse> getAccounts() {
        return accountRepository.findAll().stream().map(this::mapAccount).toList();
    }

    @Override
    public PortfolioAccountResponse updateStatus(Long portfolioAccountId, UpdatePortfolioStatusRequest request) {
        PortfolioAccount account = findAccount(portfolioAccountId);
        AccountStatus newStatus = request.accountStatus();

        if (newStatus == AccountStatus.CLOSED) {
            boolean hasOpenHoldings = holdingRepository.findByPortfolioAccountPortfolioAccountId(portfolioAccountId).stream()
                    .anyMatch(holding -> holding.getQuantity().compareTo(BigDecimal.ZERO) > 0
                            && holding.getHoldingStatus() != HoldingStatus.CLOSED);
            if (hasOpenHoldings) {
                throw new BusinessRuleException("Close or redeem all holdings before closing the portfolio account");
            }
            account.setClosedDate(LocalDate.now());
        } else {
            account.setClosedDate(null);
        }

        account.setAccountStatus(newStatus);
        return mapAccount(accountRepository.save(account));
    }

    @Override
    public PortfolioHoldingResponse addHolding(Long portfolioAccountId, CreateHoldingRequest request) {
        PortfolioAccount account = findAccount(portfolioAccountId);
        requireActiveAccount(account);
        productServiceClient.validateProduct(request.productId());

        PortfolioHolding holding = holdingRepository
                .findByPortfolioAccountPortfolioAccountIdAndProductId(portfolioAccountId, request.productId())
                .orElseGet(() -> newHolding(account, request.productId()));

        BigDecimal oldQuantity = safe(holding.getQuantity());
        BigDecimal newQuantity = oldQuantity.add(request.quantity());
        BigDecimal weightedAverageCost = PortfolioMath.weightedAverageCost(
                oldQuantity,
                safe(holding.getAverageCost()),
                request.quantity(),
                request.averageCost());

        holding.setQuantity(newQuantity);
        holding.setAverageCost(weightedAverageCost);
        holding.setMarketValue(PortfolioMath.marketValue(newQuantity, weightedAverageCost));
        holding.setHoldingStatus(HoldingStatus.ACTIVE);
        holding.setLastValuedAt(LocalDateTime.now());
        return mapHolding(holdingRepository.save(holding));
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioHoldingResponse getHolding(Long holdingId) {
        return mapHolding(findHolding(holdingId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioHoldingResponse> getHoldings(Long portfolioAccountId, HoldingStatus status) {
        findAccount(portfolioAccountId);
        List<PortfolioHolding> holdings = status == null
                ? holdingRepository.findByPortfolioAccountPortfolioAccountId(portfolioAccountId)
                : holdingRepository.findByPortfolioAccountPortfolioAccountIdAndHoldingStatus(portfolioAccountId, status);
        return holdings.stream().map(this::mapHolding).toList();
    }

    @Override
    public PortfolioHoldingResponse updateHolding(Long holdingId, UpdateHoldingRequest request) {
        PortfolioHolding holding = findHolding(holdingId);
        requireActiveAccount(holding.getPortfolioAccount());

        if (request.quantity() != null) {
            holding.setQuantity(request.quantity());
        }
        if (request.averageCost() != null) {
            holding.setAverageCost(request.averageCost());
        }
        if (request.holdingStatus() != null) {
            holding.setHoldingStatus(request.holdingStatus());
        }

        if (safe(holding.getQuantity()).compareTo(BigDecimal.ZERO) == 0) {
            holding.setHoldingStatus(HoldingStatus.CLOSED);
            holding.setMarketValue(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        } else {
            holding.setMarketValue(PortfolioMath.marketValue(holding.getQuantity(), holding.getAverageCost()));
        }
        holding.setLastValuedAt(LocalDateTime.now());
        return mapHolding(holdingRepository.save(holding));
    }

    @Override
    public void deleteHolding(Long holdingId) {
        PortfolioHolding holding = findHolding(holdingId);
        if (holding.getQuantity().compareTo(BigDecimal.ZERO) > 0 && holding.getHoldingStatus() != HoldingStatus.CLOSED) {
            throw new BusinessRuleException("A holding with remaining quantity cannot be deleted");
        }
        holdingRepository.delete(holding);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioSummaryResponse getSummary(Long portfolioAccountId) {
        PortfolioAccount account = findAccount(portfolioAccountId);
        List<PortfolioHoldingResponse> holdings = holdingRepository.findByPortfolioAccountPortfolioAccountId(portfolioAccountId)
                .stream().map(this::mapHolding).toList();

        BigDecimal totalCost = holdings.stream()
                .map(h -> h.quantity().multiply(h.averageCost()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal marketValue = holdings.stream()
                .map(PortfolioHoldingResponse::marketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal unrealized = marketValue.subtract(totalCost).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        return new PortfolioSummaryResponse(mapAccount(account), holdings, totalCost, marketValue, unrealized);
    }

    private PortfolioHolding newHolding(PortfolioAccount account, Long productId) {
        PortfolioHolding holding = new PortfolioHolding();
        holding.setPortfolioAccount(account);
        holding.setProductId(productId);
        holding.setQuantity(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        holding.setAverageCost(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        holding.setMarketValue(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        holding.setHoldingStatus(HoldingStatus.ACTIVE);
        holding.setLastValuedAt(LocalDateTime.now());
        return holding;
    }

    private PortfolioAccount findAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio account not found: " + id));
    }

    private PortfolioHolding findHolding(Long id) {
        return holdingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio holding not found: " + id));
    }

    private void requireActiveAccount(PortfolioAccount account) {
        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BusinessRuleException("Portfolio account " + account.getPortfolioAccountId() + " is not active");
        }
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private PortfolioAccountResponse mapAccount(PortfolioAccount account) {
        return new PortfolioAccountResponse(
                account.getPortfolioAccountId(),
                account.getUserId(),
                account.getAccountStatus(),
                account.getOpenedDate(),
                account.getClosedDate(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    private PortfolioHoldingResponse mapHolding(PortfolioHolding holding) {
        BigDecimal qty = safe(holding.getQuantity());
        BigDecimal avgCost = safe(holding.getAverageCost());
        BigDecimal marketValue = safe(holding.getMarketValue());
        BigDecimal unrealized = PortfolioMath.unrealizedGainLoss(qty, avgCost, marketValue);

        return new PortfolioHoldingResponse(
                holding.getHoldingId(),
                holding.getPortfolioAccount().getPortfolioAccountId(),
                holding.getProductId(),
                qty,
                avgCost,
                marketValue,
                unrealized,
                holding.getHoldingStatus(),
                holding.getLastValuedAt(),
                holding.getCreatedAt(),
                holding.getUpdatedAt()
        );
    }
}
