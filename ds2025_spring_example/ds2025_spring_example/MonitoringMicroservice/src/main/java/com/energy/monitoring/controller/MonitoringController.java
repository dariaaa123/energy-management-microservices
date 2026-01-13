package com.energy.monitoring.controller;

import com.energy.monitoring.dto.HourlyConsumptionDTO;
import com.energy.monitoring.entity.HourlyEnergyConsumption;
import com.energy.monitoring.repository.HourlyEnergyConsumptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MonitoringController {
    
    private final HourlyEnergyConsumptionRepository hourlyConsumptionRepository;
    
    @GetMapping("/devices/{deviceId}/hourly")
    public ResponseEntity<List<HourlyConsumptionDTO>> getHourlyConsumption(
            @PathVariable String deviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        List<HourlyEnergyConsumption> consumptions;
        
        if (start != null && end != null) {
            consumptions = hourlyConsumptionRepository.findByDeviceIdAndHourTimestampBetween(
                deviceId, start, end);
        } else {
            consumptions = hourlyConsumptionRepository.findByDeviceIdOrderByHourTimestampDesc(deviceId);
        }
        
        List<HourlyConsumptionDTO> dtos = consumptions.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/devices/{deviceId}/latest")
    public ResponseEntity<HourlyConsumptionDTO> getLatestConsumption(@PathVariable String deviceId) {
        List<HourlyEnergyConsumption> consumptions = 
            hourlyConsumptionRepository.findByDeviceIdOrderByHourTimestampDesc(deviceId);
        
        if (consumptions.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(convertToDTO(consumptions.get(0)));
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Monitoring Service is running");
    }
    
    private HourlyConsumptionDTO convertToDTO(HourlyEnergyConsumption entity) {
        return new HourlyConsumptionDTO(
            entity.getId(),
            entity.getDeviceId(),
            entity.getHourTimestamp(),
            entity.getTotalConsumption(),
            entity.getMeasurementCount(),
            entity.getCalculatedAt()
        );
    }
}
