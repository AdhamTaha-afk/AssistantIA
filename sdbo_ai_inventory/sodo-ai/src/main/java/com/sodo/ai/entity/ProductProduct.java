package com.sodo.ai.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_product")
public class ProductProduct {

    @Id
    private Long id;

    @Column(name = "default_code")
    private String defaultCode;

    @ManyToOne
    @JoinColumn(name = "product_tmpl_id")
    private ProductTemplate productTemplate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProductTemplate getProductTemplate() { return productTemplate; }
    public void setProductTemplate(ProductTemplate productTemplate) { this.productTemplate = productTemplate; }

    public String getDefaultCode() { return defaultCode; }
    public void setDefaultCode(String defaultCode) { this.defaultCode = defaultCode; }
}