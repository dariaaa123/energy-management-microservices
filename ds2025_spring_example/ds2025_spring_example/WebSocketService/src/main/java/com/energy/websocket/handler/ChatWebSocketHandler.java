package com.energy.websocket.handler;

import com.energy.websocket.dto.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> adminSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ChatServiceClient chatServiceClient;

    @Autowired
    public void setChatServiceClient(@Lazy ChatServiceClient chatServiceClient) {
        this.chatServiceClient = chatServiceClient;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Chat WebSocket connection established: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        userSessions.values().remove(session);
        adminSessions.values().remove(session);
        log.info("Chat WebSocket disconnected: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
            log.info("Received chat message: {}", chatMessage);

            switch (chatMessage.getType()) {
                case "REGISTER":
                    handleRegistration(session, chatMessage);
                    break;
                case "USER_MESSAGE":
                    handleUserMessage(session, chatMessage);
                    break;
                case "ADMIN_MESSAGE":
                    handleAdminMessage(session, chatMessage);
                    break;
                case "TYPING":
                    handleTypingIndicator(chatMessage);
                    break;
                default:
                    log.warn("Unknown message type: {}", chatMessage.getType());
            }
        } catch (Exception e) {
            log.error("Error handling chat message", e);
        }
    }

    private void handleRegistration(WebSocketSession session, ChatMessage message) {
        if (message.isAdmin()) {
            adminSessions.put(message.getSenderId(), session);
            log.info("Admin registered: {}", message.getSenderName());
            // Notify admin of all active user sessions
            notifyAdminOfActiveUsers();
        } else {
            userSessions.put(message.getSenderId(), session);
            log.info("User registered for chat: {}", message.getSenderName());
            // Notify admins that a user is online
            notifyAdminsUserOnline(message.getSenderId(), message.getSenderName());
        }
    }

    private void handleUserMessage(WebSocketSession session, ChatMessage message) {
        message.setTimestamp(LocalDateTime.now().toString());
        
        // Forward to chat service for processing (rule-based + AI)
        if (chatServiceClient != null) {
            chatServiceClient.processUserMessage(message);
        }
        
        // Also forward to all admins
        sendToAllAdmins(message);
    }

    private void handleAdminMessage(WebSocketSession session, ChatMessage message) {
        message.setTimestamp(LocalDateTime.now().toString());
        
        // Send to specific user
        String recipientId = message.getRecipientId();
        log.info("Admin sending message to recipientId: {}, available users: {}", recipientId, userSessions.keySet());
        
        WebSocketSession userSession = userSessions.get(recipientId);
        if (userSession != null && userSession.isOpen()) {
            sendMessage(userSession, message);
            log.info("Message sent successfully to user {}", recipientId);
        } else {
            log.warn("Could not send message to user {}: session={}, isOpen={}", 
                recipientId, userSession != null, userSession != null && userSession.isOpen());
        }
    }

    private void handleTypingIndicator(ChatMessage message) {
        if (message.isAdmin()) {
            // Admin is typing, notify the user
            WebSocketSession userSession = userSessions.get(message.getRecipientId());
            if (userSession != null && userSession.isOpen()) {
                sendMessage(userSession, message);
            }
        } else {
            // User is typing, notify all admins
            sendToAllAdmins(message);
        }
    }

    public void sendToUser(String userId, ChatMessage message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
        }
    }

    public void sendToAllAdmins(ChatMessage message) {
        log.info("Sending to all admins. Admin sessions: {}", adminSessions.keySet());
        adminSessions.values().forEach(session -> {
            if (session.isOpen()) {
                sendMessage(session, message);
                log.info("Sent message to admin session: {}", session.getId());
            } else {
                log.warn("Admin session not open: {}", session.getId());
            }
        });
    }

    private void sendMessage(WebSocketSession session, ChatMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("Failed to send message", e);
        }
    }

    private void notifyAdminOfActiveUsers() {
        ChatMessage notification = new ChatMessage();
        notification.setType("ACTIVE_USERS");
        notification.setContent(String.join(",", userSessions.keySet()));
        sendToAllAdmins(notification);
    }

    private void notifyAdminsUserOnline(String userId, String userName) {
        ChatMessage notification = new ChatMessage();
        notification.setType("USER_ONLINE");
        notification.setSenderId(userId);
        notification.setSenderName(userName);
        sendToAllAdmins(notification);
    }
}
