package com.energy.websocket.controller;

import com.energy.websocket.dto.ChatMessage;
import com.energy.websocket.handler.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ws")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatWebSocketHandler chatHandler;

    // Endpoint for Chat Service to send bot/AI responses back to users
    @PostMapping("/send-to-user")
    public ResponseEntity<Void> sendToUser(@RequestBody ChatMessage message) {
        log.info("Sending message to user {}: {}", message.getRecipientId(), message.getContent());
        chatHandler.sendToUser(message.getRecipientId(), message);
        return ResponseEntity.ok().build();
    }

    // Endpoint for Chat Service to notify admins
    @PostMapping("/send-to-admins")
    public ResponseEntity<Void> sendToAdmins(@RequestBody ChatMessage message) {
        log.info("Broadcasting message to admins: {}", message.getContent());
        chatHandler.sendToAllAdmins(message);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("WebSocket Service is running");
    }
}
