package com.example.productmicroservice.services;

import com.example.productmicroservice.Entities.InvestmentProduct;
import com.example.productmicroservice.Entities.ProductType;
import com.example.productmicroservice.Exceptions.ResourceNotFoundException;
import com.example.productmicroservice.Repositories.InvestmentProductRepository;
import com.example.productmicroservice.Repositories.ProductTypeRepository;
import com.example.productmicroservice.dto.request.CreateInvestmentProductRequest;
import com.example.productmicroservice.dto.response.InvestmentProductResponse;
import com.example.productmicroservice.enums.PriceMethod;
import com.example.productmicroservice.enums.ProductStatus;
import com.example.productmicroservice.enums.RiskCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService implements IProductService {

    private final InvestmentProductRepository investmentProductRepository;
    private final ProductTypeRepository productTypeRepository;

    public ProductService(InvestmentProductRepository investmentProductRepository,
                          ProductTypeRepository productTypeRepository) {
        this.investmentProductRepository = investmentProductRepository;
        this.productTypeRepository = productTypeRepository;
    }

    @Override
    public InvestmentProductResponse create(CreateInvestmentProductRequest request) {
        String productName = request.productName().trim();
        if (investmentProductRepository.existsByProductNameIgnoreCase(productName)) {
            throw new IllegalArgumentException("Investment product name already exists: " + productName);
        }

        InvestmentProduct product = new InvestmentProduct();
        apply(request, product, productName);
        return toResponse(investmentProductRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public InvestmentProductResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvestmentProductResponse> getAll(Long productTypeId, ProductStatus status,
                                                  RiskCategory riskCategory, PriceMethod priceMethod,
                                                  String productName) {
        return investmentProductRepository.findAll().stream()
                .filter(product -> productTypeId == null
                        || product.getProductType().getProductTypeId().equals(productTypeId))
                .filter(product -> status == null || status.name().equals(product.getStatus()))
                .filter(product -> riskCategory == null
                        || riskCategory.name().equals(product.getRiskCategory()))
                .filter(product -> priceMethod == null
                        || priceMethod.name().equals(product.getPriceMethod()))
                .filter(product -> productName == null
                        || product.getProductName().equalsIgnoreCase(productName.trim()))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public InvestmentProductResponse update(Long id, CreateInvestmentProductRequest request) {
        InvestmentProduct product = getEntity(id);
        String productName = request.productName().trim();
        if (!product.getProductName().equalsIgnoreCase(productName)
                && investmentProductRepository.existsByProductNameIgnoreCase(productName)) {
            throw new IllegalArgumentException("Investment product name already exists: " + productName);
        }

        apply(request, product, productName);
        return toResponse(investmentProductRepository.save(product));
    }

    @Override
    public void delete(Long id) {
        investmentProductRepository.delete(getEntity(id));
    }

    private void apply(CreateInvestmentProductRequest request, InvestmentProduct product,
                       String productName) {
        validateDates(request);
        ProductType productType = productTypeRepository.findById(request.productTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Product type", request.productTypeId()));

        product.setProductType(productType);
        product.setProductName(productName);
        product.setBasePrice(request.basePrice());
        product.setCurrentPrice(request.currentPrice());
        product.setMinimumInvestment(request.minimumInvestment());
        product.setRiskCategory(request.riskCategory().name());
        product.setPriceMethod(request.priceMethod().name());
        product.setTenureMonths(request.tenureMonths());
        product.setInterestRate(request.interestRate());
        product.setIssueDate(request.issueDate());
        product.setMaturityDate(request.maturityDate());
        product.setStatus((request.status() == null ? ProductStatus.ACTIVE : request.status()).name());
    }

    private void validateDates(CreateInvestmentProductRequest request) {
        if (request.issueDate() != null && request.maturityDate() != null
                && request.maturityDate().isBefore(request.issueDate())) {
            throw new IllegalArgumentException("Maturity date cannot be before issue date");
        }
    }

    private InvestmentProduct getEntity(Long id) {
        return investmentProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investment product", id));
    }

    private InvestmentProductResponse toResponse(InvestmentProduct product) {
        ProductStatus status = ProductStatus.valueOf(product.getStatus());
        return new InvestmentProductResponse(
                product.getProductId(),
                product.getProductType().getProductTypeId(),
                product.getProductType().getTypeCode(),
                product.getProductName(),
                product.getBasePrice(),
                product.getCurrentPrice(),
                product.getMinimumInvestment(),
                RiskCategory.valueOf(product.getRiskCategory()),
                PriceMethod.valueOf(product.getPriceMethod()),
                product.getTenureMonths(),
                product.getInterestRate(),
                product.getIssueDate(),
                product.getMaturityDate(),
                status,
                status == ProductStatus.ACTIVE,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
