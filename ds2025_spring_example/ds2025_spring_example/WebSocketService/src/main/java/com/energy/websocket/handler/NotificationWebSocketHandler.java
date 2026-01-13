package com.energy.websocket.handler;

import com.energy.websocket.dto.NotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    // Map userId -> WebSocket session
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = extractUserId(session);
        if (userId != null) {
            userSessions.put(userId, session);
            log.info("Notification WebSocket connected for user: {}", userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = extractUserId(session);
        if (userId != null) {
            userSessions.remove(userId);
            log.info("Notification WebSocket disconnected for user: {}", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Client can send registration message with userId
        try {
            Map<String, String> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String type = payload.get("type");
            if ("REGISTER".equals(type)) {
                String userId = payload.get("userId");
                if (userId != null) {
                    userSessions.put(userId, session);
                    log.info("User {} registered for notifications", userId);
                }
            }
        } catch (Exception e) {
            log.error("Error handling message", e);
        }
    }

    public void sendNotificationToUser(String userId, NotificationMessage notification) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(notification);
                session.sendMessage(new TextMessage(json));
                log.info("Sent notification to user {}: {}", userId, notification.getMessage());
            } catch (IOException e) {
                log.error("Failed to send notification to user {}", userId, e);
            }
        } else {
            log.warn("No active session for user {}", userId);
        }
    }

    public void broadcastToAll(NotificationMessage notification) {
        userSessions.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    String json = objectMapper.writeValueAsString(notification);
                    session.sendMessage(new TextMessage(json));
                } catch (IOException e) {
                    log.error("Failed to broadcast to user {}", userId, e);
                }
            }
        });
    }

    private String extractUserId(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.contains("userId=")) {
            return query.split("userId=")[1].split("&")[0];
        }
        return null;
    }
}
