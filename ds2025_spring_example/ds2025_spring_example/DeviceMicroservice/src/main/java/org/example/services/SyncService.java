package org.example.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.SyncMessage;
import org.example.entities.Device;
import org.example.entity.User;
import org.example.repositories.DeviceRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {
    
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    
    @Transactional
    public void processSyncMessage(SyncMessage message) {
        log.info("Processing sync message: {}", message);
        
        if ("USER".equalsIgnoreCase(message.getType())) {
            handleUserSync(message);
        } else {
            log.debug("Ignoring sync message type: {}", message.getType());
        }
    }
    
    private void handleUserSync(SyncMessage message) {
        String action = message.getAction();
        
        if ("CREATE".equalsIgnoreCase(action) || "UPDATE".equalsIgnoreCase(action)) {
            User user = userRepository.findById(message.getUserId())
                .orElse(new User());
            
            user.setUserId(message.getUserId());
            user.setUsername(message.getUsername());
            user.setName(message.getName());
            
            userRepository.save(user);
            log.info("✅ Synced user to device-db: {}", user);
            
        } else if ("DELETE".equalsIgnoreCase(action)) {
            String userId = message.getUserId();
            
            // First, unassign all devices belonging to this user
            List<Device> userDevices = deviceRepository.findByUserId(userId);
            log.info("Found {} devices for user {}", userDevices.size(), userId);
            
            if (!userDevices.isEmpty()) {
                for (Device device : userDevices) {
                    log.info("Unassigning device {} from user {}", device.getId(), userId);
                    device.setUserId(null);
                    deviceRepository.save(device);
                }
                log.info("✅ Unassigned {} devices from deleted user {}", userDevices.size(), userId);
            } else {
                log.info("No devices found for user {}", userId);
            }
            
            // Then delete the user
            userRepository.deleteById(userId);
            log.info("✅ Deleted user from device-db: {}", userId);
        }
    }
}
