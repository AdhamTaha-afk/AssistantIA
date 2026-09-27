package com.sodo.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AiDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiRequest {
        private String message;
        private String userId;
        private String odooVersion; // "v16", "v19", "16", "19"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiResponse {
        private String answer;
        private String redirectModule; // ex: "/odoo/sales", "/web#action=sale.action_orders"
        private String intent; // ex: "SALES_CA", "STOCK", "PURCHASE", "PROJECTS", "MONITORING", "GUIDE", "CHAT"
        private String odooVersion; // "v16" ou "v19"
    }

    // Structure interne pour parser la réponse JSON de Mistral
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OllamaResponse {
        private String response; 
    }
}
