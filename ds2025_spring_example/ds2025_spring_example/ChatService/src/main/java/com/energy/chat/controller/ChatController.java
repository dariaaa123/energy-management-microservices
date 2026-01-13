package com.energy.chat.controller;

import com.energy.chat.dto.ChatMessage;
import com.energy.chat.service.ChatService;
import com.energy.chat.service.HuggingFaceAIService;
import com.energy.chat.service.RuleBasedChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;
    private final RuleBasedChatService ruleBasedService;
    private final HuggingFaceAIService aiService;

    @PostMapping("/process")
    public ResponseEntity<Void> processMessage(@RequestBody ChatMessage message) {
        log.info("Received message to process: {}", message);
        chatService.processMessage(message);
        return ResponseEntity.ok().build();
    }

    // Direct endpoint for testing rule-based responses
    @PostMapping("/test-rules")
    public ResponseEntity<Map<String, String>> testRules(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        Optional<String> response = ruleBasedService.findResponse(message);
        
        if (response.isPresent()) {
            return ResponseEntity.ok(Map.of(
                "type", "RULE_BASED",
                "response", response.get()
            ));
        }
        return ResponseEntity.ok(Map.of(
            "type", "NO_MATCH",
            "response", "No rule matched"
        ));
    }

    // Direct endpoint for testing AI responses
    @PostMapping("/test-ai")
    public ResponseEntity<Map<String, String>> testAI(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        
        if (!aiService.isEnabled()) {
            return ResponseEntity.ok(Map.of(
                "type", "DISABLED",
                "response", "AI is not enabled or API key not configured"
            ));
        }

        Optional<String> response = aiService.generateResponse(message);
        if (response.isPresent()) {
            return ResponseEntity.ok(Map.of(
                "type", "AI",
                "response", response.get()
            ));
        }
        return ResponseEntity.ok(Map.of(
            "type", "ERROR",
            "response", "Failed to generate AI response"
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "running",
            "aiEnabled", aiService.isEnabled()
        ));
    }
}
