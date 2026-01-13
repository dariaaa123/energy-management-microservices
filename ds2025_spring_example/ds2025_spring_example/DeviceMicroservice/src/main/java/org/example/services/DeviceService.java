package org.example.services;


import org.example.dtos.DeviceDTO;
import org.example.dtos.DeviceDetailsDTO;
import org.example.dtos.builders.DeviceBuilder;
import org.example.entities.Device;
import org.example.handlers.exceptions.model.ResourceNotFoundException;
import org.example.repositories.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);
    private final DeviceRepository deviceRepository;
    private final SyncPublisher syncPublisher;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository, SyncPublisher syncPublisher) {
        this.deviceRepository = deviceRepository;
        this.syncPublisher = syncPublisher;
    }

    public List<DeviceDTO> findDevices() {
        List<Device> deviceList = deviceRepository.findAll();
        return deviceList.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public List<DeviceDTO> findDevicesByUserId(String userId) {
        List<Device> deviceList = deviceRepository.findByUserId(userId);
        return deviceList.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public DeviceDetailsDTO findDeviceById(UUID id) {
        Optional<Device> prosumerOptional = deviceRepository.findById(id);
        if (!prosumerOptional.isPresent()) {
            LOGGER.error("Device with id {} was not found in db", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        return DeviceBuilder.toDeviceDetailsDTO(prosumerOptional.get());
    }

    public UUID insert(DeviceDetailsDTO deviceDTO) {
        Device device = DeviceBuilder.toEntity(deviceDTO);
        device = deviceRepository.save(device);
        LOGGER.debug("Device with id {} was inserted in db", device.getId());
        
        // Publish sync message to RabbitMQ
        syncPublisher.publishDeviceSync("CREATE", device.getId().toString(), device.getName(), 
            device.getMaximumConsumptionValue(), device.getUserId());
        
        return device.getId();
    }

    public void assignDeviceToUser(UUID deviceId, String userId) {
        Optional<Device> deviceOptional = deviceRepository.findById(deviceId);
        if (!deviceOptional.isPresent()) {
            LOGGER.error("Device with id {} was not found in db", deviceId);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + deviceId);
        }
        
        Device device = deviceOptional.get();
        device.setUserId(userId);
        deviceRepository.save(device);
        LOGGER.debug("Device with id {} was assigned to user {}", deviceId, userId);
        
        // Publish sync message to RabbitMQ
        syncPublisher.publishDeviceSync("UPDATE", device.getId().toString(), device.getName(), 
            device.getMaximumConsumptionValue(), device.getUserId());
    }

    public void update(UUID id, DeviceDetailsDTO deviceDTO) {
        Optional<Device> deviceOptional = deviceRepository.findById(id);
        if (!deviceOptional.isPresent()) {
            LOGGER.error("Device with id {} was not found in db", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        
        Device device = deviceOptional.get();
        device.setName(deviceDTO.getName());
        device.setMaximumConsumptionValue(deviceDTO.getMaximumConsumptionValue());
        if (deviceDTO.getUserId() != null) {
            device.setUserId(deviceDTO.getUserId());
        }
        deviceRepository.save(device);
        LOGGER.debug("Device with id {} was updated in db", id);
        
        // Publish sync message to RabbitMQ
        syncPublisher.publishDeviceSync("UPDATE", device.getId().toString(), device.getName(), 
            device.getMaximumConsumptionValue(), device.getUserId());
    }

    public void delete(UUID id) {
        Optional<Device> deviceOptional = deviceRepository.findById(id);
        if (!deviceOptional.isPresent()) {
            LOGGER.error("Device with id {} was not found in db", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        
        Device device = deviceOptional.get();
        
        // Publish sync message to RabbitMQ before deletion
        syncPublisher.publishDeviceSync("DELETE", device.getId().toString(), device.getName(), 
            device.getMaximumConsumptionValue(), device.getUserId());
        
        deviceRepository.deleteById(id);
        LOGGER.debug("Device with id {} was deleted from db", id);
    }

}
