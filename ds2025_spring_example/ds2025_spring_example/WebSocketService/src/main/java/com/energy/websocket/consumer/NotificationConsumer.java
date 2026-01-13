package com.energy.websocket.consumer;

import com.energy.websocket.dto.NotificationMessage;
import com.energy.websocket.handler.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationWebSocketHandler notificationHandler;

    @RabbitListener(queues = "${rabbitmq.queue.notification}")
    public void consumeNotification(NotificationMessage notification) {
        log.info("Received notification from RabbitMQ: {}", notification);
        
        String userId = notification.getUserId();
        if (userId != null && !userId.isEmpty()) {
            notificationHandler.sendNotificationToUser(userId, notification);
        } else {
            // Broadcast to all if no specific user
            notificationHandler.broadcastToAll(notification);
        }
    }
}
