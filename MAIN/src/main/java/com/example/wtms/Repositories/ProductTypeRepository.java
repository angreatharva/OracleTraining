package com.example.wtms.Repositories;

import com.example.wtms.Entities.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {

    Optional<ProductType> findByTypeCode(String typeCode);

    boolean existsByTypeCode(String typeCode);

    List<ProductType> findByStatus(String status);

    List<ProductType> findByTypeName(String typeName);
}
