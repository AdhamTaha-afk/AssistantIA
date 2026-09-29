# 🚀 Guide d'Intégration du Module `sodo-ai`

Ce document centralise tout le code et l'architecture nécessaires pour intégrer l'intelligence artificielle (Ollama + Mistral) dans l'ERP SODO, avec support multi-tenant et redirection intelligente.

---

## 📦 1. Configuration Maven (`sodo-ai/pom.xml`)

Créez le dossier `sodo-ai` et ajoutez-y ce fichier `pom.xml`.
N'oubliez pas d'ajouter `<module>sodo-ai</module>` dans le `pom.xml` racine de l'ERP.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.sodo</groupId>
        <artifactId>sodo-parent</artifactId>
        <version>4.3.13</version>
    </parent>

    <artifactId>sodo-ai</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>com.sodo</groupId>
            <artifactId>sodo-core</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-high-level-client</artifactId>
        </dependency>
    </dependencies>
</project>
```

---

## 🧩 2. Les DTOs (`sodo-ai/src/main/java/com/sodo/ai/dto/AiDto.java`)

```java
package com.sodo.ai.dto;

import lombok.Builder;
import lombok.Data;

public class AiDto {

    @Data @Builder
    public static class AiRequest {
        private String message;
        private String userId; 
    }

    @Data @Builder
    public static class AiResponse {
        private String answer;
        private String redirectModule; // ex: "/accounting/tva"
        private String intent; // ex: "VIEW_INVOICE"
    }

    // Structure interne pour parser la réponse JSON de Mistral
    @Data
    public static class OllamaResponse {
        private String response; 
    }
}
```

---

## 🔍 3. Context Builder & RAG (`sodo-ai/src/main/java/com/sodo/ai/service/ContextService.java`)

Isolé par tenant pour des raisons de sécurité strictes.

```java
package com.sodo.ai.service;

import com.sodo.core.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContextService {

    // private final ElasticsearchDocumentService searchService;
    // private final InvoiceRepository invoiceRepository;

    public String buildBusinessContext(String userQuery) {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Accès refusé : Aucun contexte tenant détecté.");
        }

        StringBuilder context = new StringBuilder();
        context.append("INFORMATIONS DE L'ENTREPRISE:\n");
        context.append("- Tenant ID: ").append(tenantId).append("\n\n");

        // EXEMPLE RAG MATÉRIALISÉ : Si la requête parle de factures
        if (userQuery.toLowerCase().contains("facture")) {
            context.append("DONNÉES PERTINENTES (Dernières factures non payées) :\n");
            // var invoices = invoiceRepository.findTop5ByTenantIdAndStatus(tenantId, "UNPAID");
            // for(Invoice inv : invoices) { ... }
            context.append("- Facture FAC-2024-001 (Montant: 1200€, Statut: En retard)\n");
            context.append("- Facture FAC-2024-002 (Montant: 450€, Statut: À régler avant le 15/04)\n");
        }

        // Si la requête parle de collaborateurs (HR)
        if (userQuery.toLowerCase().contains("congé") || userQuery.toLowerCase().contains("vacances")) {
            context.append("RÈGLES RH DU TENANT :\n");
            context.append("- Politique de validation: Manager direct puis RH.\n");
            context.append("- Solde de congés actuel de l'utilisateur: 12 jours.\n");
        }

        return context.toString();
    }
}
```

---

## 🧠 4. Prompt Builder (`sodo-ai/src/main/java/com/sodo/ai/service/PromptBuilderService.java`)

Oblige le modèle à répondre au format JSON pour être lisible par notre backend.

```java
package com.sodo.ai.service;

import org.springframework.stereotype.Service;

@Service
public class PromptBuilderService {

    public String generateSystemPrompt(String businessContext, String userMessage) {
        return """
        Tu es l'assistant IA officiel de l'ERP SODO. Tu es professionnel, concis et précis.
        
        RÈGLES STRICTES :
        1. Tu dois TOUJOURS répondre au format JSON valide, sans markdown autour.
        2. Ne propose que des actions faisables dans l'ERP.
        3. Base-toi EXCLUSIVEMENT sur le contexte fourni.
        
        MODULES DE REDIRECTION POSSIBLES :
        - /accounting/tva (Déclarations TVA)
        - /accounting/fec (Génération FEC)
        - /invoices (Factures)
        - /hr/leaves (Congés)
        - /crm/leads (Pistes commerciales)
        - /documents (GED)

        CONTEXTE ERP ACTUEL : 
        """ + businessContext + """
        
        MESSAGE DE L'UTILISATEUR : 
        \"""" + userMessage + """
        \"

        FORMAT DE RÉPONSE ATTENDU (JSON) :
        {
            "answer": "Ta réponse texte en français professionnel.",
            "redirectModule": "/le/lien/du/module ou null si aucune redirection n'est pertinente",
            "intent": "Un mot clé en majuscule parmi (INVOICES, HR, ACCOUNTING, CRM, GED, CHAT)"
        }
        """;
    }
}
```

---

## 🚀 5. Appel LLM via Ollama (`sodo-ai/src/main/java/com/sodo/ai/service/AiChatService.java`)

```java
package com.sodo.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sodo.ai.dto.AiDto.AiResponse;
import com.sodo.ai.dto.AiDto.OllamaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final ContextService contextService;
    private final PromptBuilderService promptBuilder;
    private final ObjectMapper objectMapper;

    @Value("${sodo.ai.ollama.url:http://localhost:11434/api/generate}")
    private String ollamaUrl;

    @Value("${sodo.ai.model:mistral}")
    private String aiModel;

    public AiResponse processMessage(String userMessage) {
        String context = contextService.buildBusinessContext(userMessage);
        String fullPrompt = promptBuilder.generateSystemPrompt(context, userMessage);

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> body = new HashMap<>();
        body.put("model", aiModel);
        body.put("prompt", fullPrompt);
        body.put("stream", false);
        body.put("format", "json"); 

        try {
            OllamaResponse ollamaResp = restTemplate.postForObject(ollamaUrl, body, OllamaResponse.class);
            return objectMapper.readValue(ollamaResp.getResponse(), AiResponse.class);
        } catch (Exception e) {
            log.error("Erreur lors de l'appel au LLM", e);
            return AiResponse.builder()
                .answer("Désolé, je rencontre des difficultés techniques avec l'IA.")
                .intent("ERROR")
                .build();
        }
    }
}
```

---

## 🔌 6. Interception dans `sodo-chat` (`ChatWebSocketController.java`)

À ajouter dans le module existant `sodo-chat`.

```java
package com.sodo.chat.controller;

import com.sodo.ai.service.AiChatService;
import com.sodo.ai.dto.AiDto.AiResponse;
import com.sodo.core.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final AiChatService aiService;

    @MessageMapping("/chat.send")
    public void processMessage(ChatMessage incomingMessage) {
        
        // TenantContext.setTenant(incomingMessage.getTenantId());

        if ("@bot".equals(incomingMessage.getRecipientId())) {
            
            AiResponse aiResponse = aiService.processMessage(incomingMessage.getContent());

            ChatMessage botReply = new ChatMessage();
            botReply.setSenderId("@bot");
            botReply.setRecipientId(incomingMessage.getSenderId());
            botReply.setContent(aiResponse.getAnswer());
            botReply.setRedirectUrl(aiResponse.getRedirectModule()); 
            botReply.setTimestamp(System.currentTimeMillis());

            messagingTemplate.convertAndSendToUser(
                incomingMessage.getSenderId(), 
                "/queue/messages", 
                botReply
            );

        } else {
            // Chat normal...
        }
    }
}
```

---

## 🖥️ 7. Frontend (Vue.js)

```javascript
import { useRouter } from 'vue-router';

export default {
  setup() {
    const router = useRouter();

    const handleIncomingMessage = (message) => {
      chatStore.appendMessage(message);

      if (message.redirectUrl && message.senderId === '@bot') {
        router.push(message.redirectUrl);
      }
    };

    return { handleIncomingMessage };
  }
}
```
