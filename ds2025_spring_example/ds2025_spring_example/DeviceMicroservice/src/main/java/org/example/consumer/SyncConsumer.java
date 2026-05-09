package org.example.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.SyncMessage;
import org.example.services.SyncService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SyncConsumer {
    
    private final SyncService syncService;
    
    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("SyncConsumer initialized and ready to consume messages from queue: ${rabbitmq.queue.sync}");
    }
    
    @RabbitListener(queues = "${rabbitmq.queue.sync}")
    public void consumeSyncMessage(SyncMessage message) {
        try {
            log.info("Received sync message: {}", message);
            syncService.processSyncMessage(message);
        } catch (Exception e) {
            log.error("Error processing sync message: {}", message, e);
        }
    }
}
