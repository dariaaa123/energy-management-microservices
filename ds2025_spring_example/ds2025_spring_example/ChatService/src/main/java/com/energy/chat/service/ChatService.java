package com.energy.chat.service;

import com.energy.chat.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final RuleBasedChatService ruleBasedService;
    private final HuggingFaceAIService aiService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${websocket.service.url}")
    private String websocketServiceUrl;

    public void processMessage(ChatMessage message) {
        log.info("Processing message from user {}: {}", message.getSenderName(), message.getContent());

        // Step 1: Try rule-based response
        Optional<String> ruleResponse = ruleBasedService.findResponse(message.getContent());
        
        if (ruleResponse.isPresent()) {
            log.info("Found rule-based response");
            sendBotResponse(message.getSenderId(), ruleResponse.get(), "RULE_BASED");
            return;
        }

        // Step 2: Try AI response if enabled
        if (aiService.isEnabled()) {
            log.info("No rule matched, trying AI response");
            Optional<String> aiResponse = aiService.generateResponse(message.getContent());
            
            if (aiResponse.isPresent()) {
                sendBotResponse(message.getSenderId(), aiResponse.get(), "AI");
                return;
            }
        }

        // Step 3: Default fallback response
        log.info("No response found, sending fallback");
        String fallbackResponse = "I'm not sure how to help with that specific question. " +
                "Let me connect you with a human administrator who can assist you better. " +
                "In the meantime, you can try asking about:\n" +
                "- Device management\n" +
                "- Energy consumption\n" +
                "- Account issues\n" +
                "- Technical support";
        sendBotResponse(message.getSenderId(), fallbackResponse, "FALLBACK");
        
        // Notify admins that user needs help
        notifyAdminsForHelp(message);
    }

    private void sendBotResponse(String userId, String content, String responseType) {
        ChatMessage response = new ChatMessage();
        response.setType("BOT_RESPONSE");
        response.setSenderId("BOT");
        response.setSenderName("Support Bot");
        response.setRecipientId(userId);
        response.setContent(content);
        response.setTimestamp(LocalDateTime.now().toString());
        response.setAdmin(false);

        sendToWebSocketService("/api/ws/send-to-user", response);
        log.info("Sent {} response to user {}", responseType, userId);
    }

    private void notifyAdminsForHelp(ChatMessage originalMessage) {
        ChatMessage notification = new ChatMessage();
        notification.setType("HELP_NEEDED");
        notification.setSenderId(originalMessage.getSenderId());
        notification.setSenderName(originalMessage.getSenderName());
        notification.setContent("User needs assistance: " + originalMessage.getContent());
        notification.setTimestamp(LocalDateTime.now().toString());

        sendToWebSocketService("/api/ws/send-to-admins", notification);
    }

    private void sendToWebSocketService(String endpoint, ChatMessage message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ChatMessage> request = new HttpEntity<>(message, headers);
            
            restTemplate.postForEntity(websocketServiceUrl + endpoint, request, Void.class);
        } catch (Exception e) {
            log.error("Failed to send message to WebSocket service", e);
        }
    }
}
