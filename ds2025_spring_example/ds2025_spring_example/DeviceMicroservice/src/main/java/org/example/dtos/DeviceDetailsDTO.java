package org.example.dtos;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

public class DeviceDetailsDTO {

    private UUID id;

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "maximum consumption value is required")
    @Min(value = 0, message = "value must be >= 0")
    private Integer maximumConsumptionValue;

    private String userId;

    public DeviceDetailsDTO() {
    }

    public DeviceDetailsDTO(String name, int maximumConsumptionValue) {
        this.name = name;
        this.maximumConsumptionValue = maximumConsumptionValue;
    }

    public DeviceDetailsDTO(UUID id, String name, int maximumConsumptionValue, String userId) {
        this.id = id;
        this.name = name;
        this.maximumConsumptionValue = maximumConsumptionValue;
        this.userId = userId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaximumConsumptionValue() {
        return maximumConsumptionValue;
    }

    public void setMaximumConsumptionValue(int maximumConsumptionValue) {
        this.maximumConsumptionValue = maximumConsumptionValue;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceDetailsDTO that = (DeviceDetailsDTO) o;
        return maximumConsumptionValue == that.maximumConsumptionValue &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, maximumConsumptionValue);
    }
}
