package com.example.productmicroservice.services;

import com.example.productmicroservice.entities.ProductType;
import com.example.productmicroservice.exceptions.ResourceNotFoundException;
import com.example.productmicroservice.repositories.InvestmentProductRepository;
import com.example.productmicroservice.repositories.ProductTypeRepository;
import com.example.productmicroservice.dto.request.CreateProductTypeRequest;
import com.example.productmicroservice.dto.response.ProductTypeResponse;
import com.example.productmicroservice.enums.ProductStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class ProductTypeService implements IProductTypeService {

    private final ProductTypeRepository productTypeRepository;
    private final InvestmentProductRepository investmentProductRepository;

    public ProductTypeService(ProductTypeRepository productTypeRepository,
                              InvestmentProductRepository investmentProductRepository) {
        this.productTypeRepository = productTypeRepository;
        this.investmentProductRepository = investmentProductRepository;
    }

    @Override
    public ProductTypeResponse create(CreateProductTypeRequest request) {
        String typeCode = normalizeCode(request.typeCode());
        if (productTypeRepository.existsByTypeCodeIgnoreCase(typeCode)) {
            throw new IllegalArgumentException("Product type code already exists: " + typeCode);
        }

        ProductType productType = new ProductType();
        apply(request, productType, typeCode);
        return toResponse(productTypeRepository.save(productType));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductTypeResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductTypeResponse> getAll(ProductStatus status, String typeName) {
        return productTypeRepository.findAll().stream()
                .filter(type -> status == null || status.name().equals(type.getStatus()))
                .filter(type -> typeName == null || type.getTypeName().equalsIgnoreCase(typeName.trim()))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ProductTypeResponse update(Long id, CreateProductTypeRequest request) {
        ProductType productType = getEntity(id);
        String typeCode = normalizeCode(request.typeCode());
        if (!productType.getTypeCode().equalsIgnoreCase(typeCode)
                && productTypeRepository.existsByTypeCodeIgnoreCase(typeCode)) {
            throw new IllegalArgumentException("Product type code already exists: " + typeCode);
        }

        apply(request, productType, typeCode);
        return toResponse(productTypeRepository.save(productType));
    }

    @Override
    public void delete(Long id) {
        ProductType productType = getEntity(id);
        if (investmentProductRepository.existsByProductType_ProductTypeId(id)) {
            throw new IllegalStateException("Product type cannot be deleted while investment products use it");
        }
        productTypeRepository.delete(productType);
    }

    private void apply(CreateProductTypeRequest request, ProductType productType, String typeCode) {
        productType.setTypeCode(typeCode);
        productType.setTypeName(request.typeName().trim());
        productType.setDescription(request.description() == null ? null : request.description().trim());
        productType.setStatus((request.status() == null ? ProductStatus.ACTIVE : request.status()).name());
    }

    private String normalizeCode(String typeCode) {
        return typeCode.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    private ProductType getEntity(Long id) {
        return productTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product type", id));
    }

    private ProductTypeResponse toResponse(ProductType productType) {
        return new ProductTypeResponse(
                productType.getProductTypeId(),
                productType.getTypeCode(),
                productType.getTypeName(),
                productType.getDescription(),
                ProductStatus.valueOf(productType.getStatus()),
                productType.getCreatedAt(),
                productType.getUpdatedAt()
        );
    }
}
