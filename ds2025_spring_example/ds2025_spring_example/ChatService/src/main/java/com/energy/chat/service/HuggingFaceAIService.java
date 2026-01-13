package com.energy.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class HuggingFaceAIService {

    @Value("${huggingface.api.token:}")
    private String apiToken;

    @Value("${ai.enabled:false}")
    private boolean aiEnabled;

    private final RestTemplate restTemplate = new RestTemplate();

    // Hugging Face Router - OpenAI compatible endpoint
    private static final String API_URL = "https://router.huggingface.co/v1/chat/completions";
    private static final String MODEL = "Qwen/Qwen2.5-72B-Instruct";

    private static final String SYSTEM_PROMPT = 
        "You are a helpful customer support assistant for an Energy Management System. " +
        "Help users with energy consumption questions, device management, and technical support. " +
        "Keep responses concise (2-3 sentences) and friendly. " +
        "If unsure, suggest contacting an administrator.";

    public Optional<String> generateResponse(String userMessage) {
        if (!aiEnabled) {
            log.info("AI is disabled");
            return Optional.empty();
        }

        if (apiToken == null || apiToken.isEmpty()) {
            log.info("API token not configured");
            return Optional.empty();
        }

        try {
            return callHuggingFaceAPI(userMessage);
        } catch (Exception e) {
            log.error("Hugging Face API failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> callHuggingFaceAPI(String userMessage) {
        // Build OpenAI-compatible request
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);
        requestBody.put("max_tokens", 150);
        
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        messages.add(Map.of("role", "user", "content", userMessage));
        requestBody.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        log.info("Calling Hugging Face API with model: {}", MODEL);
        ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> body = response.getBody();
            log.info("Hugging Face response received");
            
            // Parse OpenAI-compatible response
            List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                if (message != null) {
                    String content = (String) message.get("content");
                    if (content != null && !content.trim().isEmpty()) {
                        log.info("AI response generated successfully");
                        return Optional.of(content.trim());
                    }
                }
            }
        }
        
        log.warn("Could not parse Hugging Face response");
        return Optional.empty();
    }

    public boolean isEnabled() {
        return aiEnabled && apiToken != null && !apiToken.isEmpty();
    }
}
