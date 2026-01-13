package com.energy.monitoring.consumer;

import com.energy.monitoring.dto.DeviceDataMessage;
import com.energy.monitoring.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceDataConsumer {
    
    private final MonitoringService monitoringService;
    
    @RabbitListener(queues = "${rabbitmq.queue.device-data}")
    public void consumeDeviceData(DeviceDataMessage message) {
        try {
            log.info("Received device data message: {}", message);
            monitoringService.processDeviceData(message);
        } catch (Exception e) {
            log.error("Error processing device data message: {}", message, e);
        }
    }
}
