package com.energy.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDataMessage {
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("deviceId")
    private String deviceId;
    
    @JsonProperty("measurementValue")
    private Double measurementValue;
}
