package com.energy.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourlyConsumptionDTO {
    private Long id;
    private String deviceId;
    private LocalDateTime hourTimestamp;
    private Double totalConsumption;
    private Integer measurementCount;
    private LocalDateTime calculatedAt;
}
