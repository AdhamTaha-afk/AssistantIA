package com.sodo.ai.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_location")
public class StockLocation {

    @Id
    private Long id;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}