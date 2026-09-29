package com.sodo.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sodo.ai.dto.AiDto.AiResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiChatService {
    private final ChatClient chatClient;
    private final ContextService contextService;
    private final PromptBuilderService promptBuilder;
    private final ObjectMapper objectMapper;
    private final org.springframework.ai.vectorstore.VectorStore vectorStore;
    private final ConversationMemoryService conversationMemory;

    public AiChatService(ChatClient.Builder chatClientBuilder, ContextService contextService,
            PromptBuilderService promptBuilder, ObjectMapper objectMapper, org.springframework.ai.vectorstore.VectorStore vectorStore,
            ConversationMemoryService conversationMemory) {
        this.chatClient = chatClientBuilder.build();
        this.contextService = contextService;
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
        this.vectorStore = vectorStore;
        this.conversationMemory = conversationMemory;
    }

    public AiResponse chat(String userMessage) {
        return chat(userMessage, null, null);
    }

    public AiResponse chat(String userMessage, String odooVersion) {
        return chat(userMessage, odooVersion, null);
    }

    public AiResponse chat(String userMessage, String odooVersion, String sessionId) {
        String version = contextService.resolveOdooVersion(userMessage, odooVersion);

        // 1. Recherche RAG documentaire
        String ragContext = "";
        try {
            log.info("Recherche de contexte RAG pour: {}", userMessage);
            List<org.springframework.ai.document.Document> similarDocuments = vectorStore.similaritySearch(
                    org.springframework.ai.vectorstore.SearchRequest.query(userMessage).withTopK(3)
            );
            ragContext = similarDocuments.stream()
                    .map(org.springframework.ai.document.Document::getContent)
                    .collect(java.util.stream.Collectors.joining("\n- "));
        } catch (Exception e) {
            log.warn("Recherche RAG vectorStore non disponible : {}", e.getMessage());
        }

        // 2. Préparation du contexte métier combiné Odoo (avec l'historique de la session pour résoudre les questions de suivi)
        List<Message> sessionHistory = conversationMemory.getHistory(sessionId);
        StringBuilder historyText = new StringBuilder();
        for (Message hm : sessionHistory) {
            historyText.append(hm.getContent()).append("\n");
        }
        String businessContext = contextService.buildBusinessContext(userMessage, version, historyText.toString());
        String finalContext = businessContext + (!ragContext.isEmpty() ? "\n\nCONNAISSANCES GUIDE ODOO :\n- " + ragContext : "");

        String systemPrompt = promptBuilder.generateSystemPrompt(finalContext);

        log.info("=== CONTEXTE MÉTIER ENVOYÉ AU LLM ===\n{}", finalContext);
        log.info("Appel Mistral avec message Odoo ({}) : {}", version, userMessage);

        try {
            // 3. Construction de la liste de messages : system + historique de la session + message actuel
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(systemPrompt));
            List<Message> history = conversationMemory.getHistory(sessionId);
            log.info("=== sessionId reçu: [{}] | Taille historique: {} ===", sessionId, history.size());
            messages.addAll(history);
            messages.add(new UserMessage(userMessage));
            
            String rawResponse = chatClient.prompt()
                    .messages(messages)
                    .call()
                    .content();

            log.info("Réponse brute de Mistral: {}", rawResponse);

            if (rawResponse != null && !rawResponse.trim().isEmpty()) {
                String cleanJson = cleanJsonResponse(rawResponse);
                AiResponse response = objectMapper.readValue(cleanJson, AiResponse.class);
                if (response.getOdooVersion() == null) {
                    response.setOdooVersion(version);
                }
                // 4. Sauvegarde de l'échange dans la mémoire de la session
                conversationMemory.addExchange(sessionId, userMessage, response.getAnswer());
                return response;
            }
        } catch (Exception e) {
            log.warn("Erreur lors de l'appel au LLM ({}), génération de la synthèse Odoo directe.", e.getMessage());
        }

        // Réponse enrichie directe basée sur les données Odoo
        AiResponse fallback = generateDirectOdooResponse(userMessage, businessContext, version);
        conversationMemory.addExchange(sessionId, userMessage, fallback.getAnswer());
        return fallback;
    }

    private AiResponse generateDirectOdooResponse(String userMessage, String businessContext, String version) {
        String q = userMessage.toLowerCase();
        String redirect = "/web#action=sale.action_orders";
        String intent = "SALES_CA";

        if (q.contains("stock") || q.contains("inventaire") || q.contains("rupture")) {
            redirect = "/web#action=stock.action_picking_tree_all";
            intent = "STOCK";
        } else if (q.contains("achat") || q.contains("fournisseur")) {
            redirect = "/web#action=purchase.purchase_rfq";
            intent = "PURCHASES";
        } else if (q.contains("projet") || q.contains("tâche") || q.contains("organisation")) {
            redirect = "/web#action=project.open_view_project_all";
            intent = "PROJECTS";
        } else if (q.contains("monitoring") || q.contains("kpi") || q.contains("dashboard")) {
            redirect = "/web#action=base.action_partner_dashboard";
            intent = "MONITORING";
        }

        String answer = "Voici les informations et indicateurs clés extraits de votre environnement Odoo " + version.toUpperCase() + " :\n\n" + businessContext;

        return AiResponse.builder()
                .answer(answer)
                .intent(intent)
                .redirectModule(redirect)
                .odooVersion(version)
                .build();
    }

    private String cleanJsonResponse(String response) {
        if (response == null)
            return "{}";
        String clean = response.trim();

        if (clean.startsWith("```json")) {
            clean = clean.substring(7);
        } else if (clean.startsWith("```")) {
            clean = clean.substring(3);
        }

        if (clean.endsWith("```")) {
            clean = clean.substring(0, clean.length() - 3);
        }

        return clean.trim();
    }
}