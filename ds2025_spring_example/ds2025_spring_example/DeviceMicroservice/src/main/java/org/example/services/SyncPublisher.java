package org.example.services;

import org.example.dto.SyncMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SyncPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncPublisher.class);
    
    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.exchange.sync:sync_exchange}")
    private String syncExchange;
    
    @Value("${rabbitmq.routing-key.sync:sync.key}")
    private String syncRoutingKey;
    
    public void publishDeviceSync(String action, String deviceId, String deviceName, 
                                   int maximumConsumptionValue, String assignedUserId) {
        if (rabbitTemplate == null) {
            LOGGER.warn("RabbitTemplate not available, skipping sync message");
            return;
        }
        
        try {
            SyncMessage message = new SyncMessage();
            message.setType("DEVICE");
            message.setAction(action);
            message.setDeviceId(deviceId);
            message.setDeviceName(deviceName);
            message.setMaximumConsumptionValue((double) maximumConsumptionValue);
            message.setAssignedUserId(assignedUserId);
            
            rabbitTemplate.convertAndSend(syncExchange, syncRoutingKey, message);
            LOGGER.info("Published device sync message: {} for device {}", action, deviceId);
        } catch (Exception e) {
            LOGGER.error("Failed to publish device sync message: {}", e.getMessage(), e);
        }
    }
}
