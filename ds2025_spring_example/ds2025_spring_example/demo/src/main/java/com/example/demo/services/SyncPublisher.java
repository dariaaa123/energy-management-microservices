package com.example.demo.services;

import com.example.demo.dto.SyncMessage;
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
    
    public void publishUserSync(String action, String userId, String username, String name) {
        LOGGER.info("publishUserSync called: action={}, userId={}, username={}", action, userId, username);
        
        if (rabbitTemplate == null) {
            LOGGER.warn("RabbitTemplate not available, skipping sync message");
            return;
        }
        
        LOGGER.info("RabbitTemplate is available, publishing to exchange={}, routingKey={}", syncExchange, syncRoutingKey);
        
        try {
            SyncMessage message = new SyncMessage();
            message.setType("USER");
            message.setAction(action);
            message.setUserId(userId);
            message.setUsername(username);
            message.setName(name);
            
            rabbitTemplate.convertAndSend(syncExchange, syncRoutingKey, message);
            LOGGER.info("Published user sync message: {} for user {}", action, userId);
        } catch (Exception e) {
            LOGGER.error("Failed to publish user sync message: {}", e.getMessage(), e);
        }
    }
}
