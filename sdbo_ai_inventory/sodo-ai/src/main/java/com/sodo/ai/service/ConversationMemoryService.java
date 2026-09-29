package com.sodo.ai.service;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ConversationMemoryService {

    // Nombre maximal d'échanges (question+réponse) conservés par session, pour éviter une croissance illimitée.
    private static final int MAX_EXCHANGES_PER_SESSION = 10;

    private final Map<String, CopyOnWriteArrayList<Message>> sessions = new ConcurrentHashMap<>();

    /**
     * Retourne l'historique des messages pour une session donnée (liste vide si inconnue ou sessionId null).
     */
    public List<Message> getHistory(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return List.of();
        }
        return sessions.getOrDefault(sessionId, new CopyOnWriteArrayList<>());
    }

    /**
     * Enregistre un nouvel échange (question utilisateur + réponse assistant) dans la session.
     * Ne fait rien si sessionId est null (mode sans mémoire).
     */
    public void addExchange(String sessionId, String userMessage, String assistantAnswer) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return;
        }
        CopyOnWriteArrayList<Message> history = sessions.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>());
        history.add(new UserMessage(userMessage));
        history.add(new AssistantMessage(assistantAnswer));

        // Limiter la taille : on garde au maximum MAX_EXCHANGES_PER_SESSION échanges (2 messages par échange).
        int maxMessages = MAX_EXCHANGES_PER_SESSION * 2;
        while (history.size() > maxMessages) {
            history.remove(0);
        }
    }
}