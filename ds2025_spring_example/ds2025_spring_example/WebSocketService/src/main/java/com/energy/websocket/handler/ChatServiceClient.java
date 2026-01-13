package com.energy.websocket.handler;

import com.energy.websocket.dto.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class ChatServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${chat.service.url:http://chat-service:8086}")
    private String chatServiceUrl;

    @Async
    public void processUserMessage(ChatMessage message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ChatMessage> request = new HttpEntity<>(message, headers);
            
            restTemplate.postForEntity(
                chatServiceUrl + "/api/chat/process",
                request,
                Void.class
            );
            log.info("Forwarded message to chat service for processing");
        } catch (Exception e) {
            log.error("Failed to forward message to chat service", e);
        }
    }
}
