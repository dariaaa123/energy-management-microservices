package com.energy.monitoring.service;

import com.energy.monitoring.dto.DeviceDataMessage;
import com.energy.monitoring.entity.Device;
import com.energy.monitoring.entity.DeviceMeasurement;
import com.energy.monitoring.entity.HourlyEnergyConsumption;
import com.energy.monitoring.repository.DeviceMeasurementRepository;
import com.energy.monitoring.repository.DeviceRepository;
import com.energy.monitoring.repository.HourlyEnergyConsumptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {
    
    private final DeviceMeasurementRepository measurementRepository;
    private final HourlyEnergyConsumptionRepository hourlyConsumptionRepository;
    private final DeviceRepository deviceRepository;
    private final NotificationPublisher notificationPublisher;
    
    @Transactional
    public void processDeviceData(DeviceDataMessage message) {
        log.info("Processing device data: {}", message);
        
        // Parse timestamp
        LocalDateTime timestamp = parseTimestamp(message.getTimestamp());
        
        // Save raw measurement
        DeviceMeasurement measurement = new DeviceMeasurement();
        measurement.setDeviceId(message.getDeviceId());
        measurement.setTimestamp(timestamp);
        measurement.setMeasurementValue(message.getMeasurementValue());
        measurement.setReceivedAt(LocalDateTime.now());
        
        measurementRepository.save(measurement);
        log.info("Saved measurement: {}", measurement);
        
        // Calculate hourly consumption and check for overconsumption
        Double hourlyConsumption = calculateHourlyConsumption(message.getDeviceId(), timestamp);
        
        // Check for overconsumption
        checkOverconsumption(message.getDeviceId(), hourlyConsumption);
    }
    
    private Double calculateHourlyConsumption(String deviceId, LocalDateTime measurementTime) {
        // Get the start of the hour
        LocalDateTime hourStart = measurementTime.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime hourEnd = hourStart.plusHours(1);
        
        log.info("Calculating hourly consumption for device {} at hour {}", deviceId, hourStart);
        
        // Sum all measurements in this hour
        Double totalConsumption = measurementRepository.sumMeasurementsByDeviceAndTimeRange(
            deviceId, hourStart, hourEnd);
        
        Integer measurementCount = measurementRepository.countMeasurementsByDeviceAndTimeRange(
            deviceId, hourStart, hourEnd);
        
        if (totalConsumption == null) {
            totalConsumption = 0.0;
        }
        
        if (measurementCount == null) {
            measurementCount = 0;
        }
        
        // Update or create hourly record
        HourlyEnergyConsumption hourlyRecord = hourlyConsumptionRepository
            .findByDeviceIdAndHourTimestamp(deviceId, hourStart)
            .orElse(new HourlyEnergyConsumption());
        
        hourlyRecord.setDeviceId(deviceId);
        hourlyRecord.setHourTimestamp(hourStart);
        hourlyRecord.setTotalConsumption(totalConsumption);
        hourlyRecord.setMeasurementCount(measurementCount);
        hourlyRecord.setCalculatedAt(LocalDateTime.now());
        
        hourlyConsumptionRepository.save(hourlyRecord);
        log.info("Updated hourly consumption: {}", hourlyRecord);
        
        return totalConsumption;
    }
    
    private void checkOverconsumption(String deviceId, Double currentConsumption) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        
        if (deviceOpt.isEmpty()) {
            log.warn("Device {} not found in database, skipping overconsumption check", deviceId);
            return;
        }
        
        Device device = deviceOpt.get();
        Double maxAllowed = device.getMaximumConsumptionValue();
        
        if (maxAllowed == null || maxAllowed <= 0) {
            log.debug("No max consumption set for device {}", deviceId);
            return;
        }
        
        if (currentConsumption > maxAllowed) {
            log.warn("⚠️ OVERCONSUMPTION DETECTED! Device: {}, Current: {}, Max: {}", 
                deviceId, currentConsumption, maxAllowed);
            
            String userId = device.getUserId();
            if (userId != null && !userId.isEmpty()) {
                notificationPublisher.publishOverconsumptionAlert(
                    userId, deviceId, device.getName(), currentConsumption, maxAllowed
                );
            } else {
                log.warn("Device {} has no assigned user, cannot send notification", deviceId);
            }
        }
    }
    
    private LocalDateTime parseTimestamp(String timestamp) {
        try {
            // Try ISO format first
            return LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            log.warn("Failed to parse timestamp {}, using current time", timestamp);
            return LocalDateTime.now();
        }
    }
}
