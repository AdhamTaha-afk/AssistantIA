package com.sodo.ai.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "stock_quant")
public class StockQuant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private ProductProduct product;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private StockLocation location;

    @ManyToOne
    @JoinColumn(name = "package_id")
    private StockQuantPackage stockPackage;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProductProduct getProduct() { return product; }
    public void setProduct(ProductProduct product) { this.product = product; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public StockLocation getLocation() { return location; }
    public void setLocation(StockLocation location) { this.location = location; }

    public StockQuantPackage getStockPackage() { return stockPackage; }
    public void setStockPackage(StockQuantPackage stockPackage) { this.stockPackage = stockPackage; }
}