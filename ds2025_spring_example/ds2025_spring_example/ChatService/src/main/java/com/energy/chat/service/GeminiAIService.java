package com.energy.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class GeminiAIService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${ai.enabled:false}")
    private boolean aiEnabled;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String SYSTEM_CONTEXT = """
        You are a helpful customer support assistant for an Energy Management System.
        Your role is to help users with:
        - Understanding their energy consumption
        - Device management questions
        - Account and login issues
        - Technical support
        - General inquiries about the system
        
        Keep responses concise, friendly, and helpful.
        If you don't know something specific about the user's account, suggest they contact an administrator.
        """;

    public Optional<String> generateResponse(String userMessage) {
        if (!aiEnabled || apiKey == null || apiKey.isEmpty()) {
            log.info("AI is disabled or API key not configured");
            return Optional.empty();
        }

        try {
            String fullPrompt = SYSTEM_CONTEXT + "\n\nUser question: " + userMessage;
            
            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            List<Map<String, String>> parts = new ArrayList<>();
            parts.add(Map.of("text", fullPrompt));
            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            String url = apiUrl + "?key=" + apiKey;
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> contentResponse = (Map<String, Object>) candidate.get("content");
                    List<Map<String, String>> partsResponse = (List<Map<String, String>>) contentResponse.get("parts");
                    if (partsResponse != null && !partsResponse.isEmpty()) {
                        String text = partsResponse.get(0).get("text");
                        log.info("Gemini AI response generated successfully");
                        return Optional.of(text);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
        }

        return Optional.empty();
    }

    public boolean isEnabled() {
        return aiEnabled && apiKey != null && !apiKey.isEmpty();
    }
}
