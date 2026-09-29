package com.sodo.ai.controller;

import com.sodo.ai.entity.StockQuant;
import com.sodo.ai.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/inventory")
@CrossOrigin(origins = "*") // Allow requests from Odoo UI / frontend
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    // Endpoint to get all current inventory quants
    @GetMapping("/quants")
    public ResponseEntity<List<StockQuant>> getAllQuants() {
        List<StockQuant> quants = inventoryService.getAllInventoryQuants();
        return ResponseEntity.ok(quants);
    }

    // Endpoint to search inventory by product name (e.g., "Bois rouge 2m")
    @GetMapping("/search")
    public ResponseEntity<List<StockQuant>> getStockByProduct(@RequestParam String name) {
        List<StockQuant> quants = inventoryService.getStockByProductName(name);
        return ResponseEntity.ok(quants);
    }

    // Endpoint to get package-specific inventory details (e.g., DEMO-BR-001)
    @GetMapping("/packages")
    public ResponseEntity<List<StockQuant>> getPackagesInventory() {
        List<StockQuant> quants = inventoryService.getPackagesInventory();
        return ResponseEntity.ok(quants);
    }
}