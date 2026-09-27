package com.sodo.ai.controller;

import com.sodo.ai.dto.AiDto.AiRequest;
import com.sodo.ai.dto.AiDto.AiResponse;
import com.sodo.ai.odoo.OdooAnalyticsService;
import com.sodo.ai.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiChatService aiChatService;
    private final OdooAnalyticsService odooAnalytics;

    @PostMapping(path = "/chat", consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
    public AiResponse chat(@RequestBody AiRequest request) {

        String message = request.getMessage();

        if (message == null || message.isEmpty()) {
            return AiResponse.builder()
                    .answer("Message vide. Veuillez poser votre question.")
                    .intent("ERROR")
                    .redirectModule(null)
                    .build();
        }

        try {
            return aiChatService.chat(message, request.getOdooVersion());
        } catch (Exception e) {
            e.printStackTrace();
            return AiResponse.builder()
                    .answer("Erreur lors du traitement de la requête.")
                    .intent("ERROR")
                    .redirectModule(null)
                    .build();
        }
    }

    @GetMapping(path = "/odoo/monitoring", produces = "application/json;charset=UTF-8")
    public String getOdooMonitoring(@RequestParam(defaultValue = "v19") String version) {
        return odooAnalytics.getGlobalMonitoring(version);
    }
}
