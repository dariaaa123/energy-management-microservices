package org.example.dtos;

import java.util.Objects;
import java.util.UUID;

public class DeviceDTO {
    private UUID id;
    private String name;
    private Integer maximumConsumptionValue;
    private String userId;

    public DeviceDTO() {}
    public DeviceDTO(UUID id, String name, int maximumConsumptionValue, String userId) {
        this.id = id; this.name = name; this.maximumConsumptionValue = maximumConsumptionValue; this.userId = userId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMaximumConsumptionValue() { return maximumConsumptionValue; }
    public void setMaximumConsumptionValue(int maximumConsumptionValue) { this.maximumConsumptionValue = maximumConsumptionValue; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceDTO that = (DeviceDTO) o;
        return maximumConsumptionValue == that.maximumConsumptionValue && Objects.equals(name, that.name);
    }
    @Override public int hashCode() { return Objects.hash(name, maximumConsumptionValue); }
}
