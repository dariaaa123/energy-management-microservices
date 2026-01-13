package com.energy.monitoring.service;

import com.energy.monitoring.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.notification:notification_exchange}")
    private String notificationExchange;

    @Value("${rabbitmq.routing.notification:notification.key}")
    private String notificationRoutingKey;

    public void publishOverconsumptionAlert(String userId, String deviceId, String deviceName,
                                            Double currentConsumption, Double maxAllowed) {
        NotificationMessage notification = new NotificationMessage();
        notification.setType("OVERCONSUMPTION_ALERT");
        notification.setUserId(userId);
        notification.setDeviceId(deviceId);
        notification.setDeviceName(deviceName);
        notification.setCurrentConsumption(currentConsumption);
        notification.setMaxAllowed(maxAllowed);
        notification.setMessage(String.format(
            "⚠️ Alert: Device '%s' exceeded consumption limit! Current: %.2f kWh, Max allowed: %.2f kWh",
            deviceName, currentConsumption, maxAllowed
        ));
        notification.setTimestamp(LocalDateTime.now().toString());

        try {
            rabbitTemplate.convertAndSend(notificationExchange, notificationRoutingKey, notification);
            log.info("Published overconsumption alert for device {} to user {}", deviceId, userId);
        } catch (Exception e) {
            log.error("Failed to publish notification", e);
        }
    }
}
