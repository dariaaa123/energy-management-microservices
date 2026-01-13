package com.energy.monitoring.consumer;

import com.energy.monitoring.dto.SyncMessage;
import com.energy.monitoring.service.SyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SyncConsumer {
    
    private final SyncService syncService;
    
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
