package com.sodo.ai.service;

import com.sodo.ai.entity.StockQuant;
import com.sodo.ai.repository.StockQuantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InventoryService {

    @Autowired
    private StockQuantRepository stockQuantRepository;

    // جلب كل كميات المخزون الحالية
    public List<StockQuant> getAllInventoryQuants() {
        return stockQuantRepository.findAll();
    }

    // البحث عن كمية منتج معين بالاسم (مثل "Bois rouge 2m")
    public List<StockQuant> getStockByProductName(String productName) {
        return stockQuantRepository.findByProductNameContaining(productName);
    }

    // جلب الطرود الموجودة في المخزون مع كمياتها (مثل طرود DEMO-BR-001 وغيرها)
    public List<StockQuant> getPackagesInventory() {
        return stockQuantRepository.findAllInPackages();
    }
}