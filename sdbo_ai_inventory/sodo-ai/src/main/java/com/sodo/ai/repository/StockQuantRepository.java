package com.sodo.ai.repository;

import com.sodo.ai.entity.StockQuant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockQuantRepository extends JpaRepository<StockQuant, Long> {

    List<StockQuant> findByProductId(Long productId);

    @Query("SELECT q FROM StockQuant q JOIN q.product p JOIN p.productTemplate t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :productName, '%'))")
    List<StockQuant> findByProductNameContaining(@Param("productName") String productName);

    @Query("SELECT q FROM StockQuant q WHERE q.stockPackage IS NOT NULL")
    List<StockQuant> findAllInPackages();
}