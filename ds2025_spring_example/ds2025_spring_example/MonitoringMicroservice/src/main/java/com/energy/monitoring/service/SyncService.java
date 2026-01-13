package com.energy.monitoring.service;

import com.energy.monitoring.dto.SyncMessage;
import com.energy.monitoring.entity.Device;
import com.energy.monitoring.repository.DeviceRepository;
import com.energy.monitoring.repository.DeviceMeasurementRepository;
import com.energy.monitoring.repository.HourlyEnergyConsumptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {
    
    private final DeviceRepository deviceRepository;
    private final DeviceMeasurementRepository measurementRepository;
    private final HourlyEnergyConsumptionRepository hourlyConsumptionRepository;
    
    @Transactional
    public void processSyncMessage(SyncMessage message) {
        log.info("Processing sync message: {}", message);
        
        if ("DEVICE".equalsIgnoreCase(message.getType())) {
            handleDeviceSync(message);
        } else if ("USER".equalsIgnoreCase(message.getType())) {
            log.debug("Ignoring USER sync message - MonitoringService only tracks devices");
        } else {
            log.warn("Unknown sync message type: {}", message.getType());
        }
    }
    
    private void handleDeviceSync(SyncMessage message) {
        String action = message.getAction();
        
        if ("CREATE".equalsIgnoreCase(action) || "UPDATE".equalsIgnoreCase(action)) {
            Device device = deviceRepository.findById(message.getDeviceId())
                .orElse(new Device());
            
            device.setDeviceId(message.getDeviceId());
            device.setName(message.getDeviceName());
            device.setMaximumConsumptionValue(message.getMaximumConsumptionValue());
            device.setUserId(message.getAssignedUserId());
            
            deviceRepository.save(device);
            log.info("✅ Synced device: {}", device);
            
        } else if ("DELETE".equalsIgnoreCase(action)) {
            String deviceId = message.getDeviceId();
            
            // Delete all related data (cascade)
            measurementRepository.deleteByDeviceId(deviceId);
            hourlyConsumptionRepository.deleteByDeviceId(deviceId);
            deviceRepository.deleteById(deviceId);
            
            log.info("✅ Deleted device and all related data: {}", deviceId);
        }
    }
}
