package org.example.dtos.builders;

import org.example.dtos.DeviceDTO;
import org.example.dtos.DeviceDetailsDTO;
import org.example.entities.Device;

public class DeviceBuilder {

    private DeviceBuilder() {
    }

    public static DeviceDTO toDeviceDTO(Device device) {
        return new DeviceDTO(device.getId(), device.getName(), device.getMaximumConsumptionValue(), device.getUserId());
    }

    public static DeviceDetailsDTO toDeviceDetailsDTO(Device device) {
        return new DeviceDetailsDTO(device.getId(), device.getName(), device.getMaximumConsumptionValue(), device.getUserId());
    }

    public static Device toEntity(DeviceDetailsDTO userDevicesDTO) {
        if (userDevicesDTO.getUserId() != null) {
            return new Device(userDevicesDTO.getName(),
                    userDevicesDTO.getMaximumConsumptionValue(),
                    userDevicesDTO.getUserId());
        } else {
            return new Device(userDevicesDTO.getName(),
                    userDevicesDTO.getMaximumConsumptionValue());
        }
    }
}
