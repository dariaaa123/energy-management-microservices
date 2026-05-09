package org.example.consumer;

import org.example.dto.SyncMessage;
import org.example.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SyncConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(SyncConsumer.class);
    private final SyncService syncService;
    
    public SyncConsumer(SyncService syncService) {
        this.syncService = syncService;
    }
    
    @RabbitListener(queues = "${rabbitmq.queue.sync}")
    public void consumeSyncMessage(SyncMessage message) {
        try {
            log.info(" Received sync message: {}", message);
            syncService.processSyncMessage(message);
        } catch (Exception e) {
            log.error(" Error processing sync message: {}", message, e);
        }
    }
}
