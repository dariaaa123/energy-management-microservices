package org.example.controllers;

import org.example.dtos.DeviceDTO;
import org.example.dtos.DeviceDetailsDTO;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.services.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
@Validated
public class DeviceController {

    private final DeviceService deviceService;
    private final UserRepository userRepository;

    public DeviceController(DeviceService userService, UserRepository userRepository) {
        this.deviceService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getDevices() {
        return ResponseEntity.ok(deviceService.findDevices());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Void> create(
            @Valid @RequestBody DeviceDetailsDTO device,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        
        System.out.println("Received role header: '" + role + "'");
        
        // Only ADMIN can create devices
        if (role == null || !role.equals("ADMIN")) {
            System.out.println("Access denied. Role is: " + role);
            return ResponseEntity.status(403).build();
        }
        
        System.out.println("Admin access granted");
        UUID id = deviceService.insert(device);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{deviceId}/assign/{userId}")
    public ResponseEntity<Void> assignDeviceToUser(
            @PathVariable UUID deviceId,
            @PathVariable String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        
        // Only ADMIN can assign devices
        if (role == null || !role.equals("ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        
        deviceService.assignDeviceToUser(deviceId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceDetailsDTO> getDevice(@PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.findDeviceById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable UUID id,
            @Valid @RequestBody DeviceDetailsDTO device,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        
        // Only ADMIN can update devices
        if (role == null || !role.equals("ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        
        deviceService.update(id, device);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        
        // Only ADMIN can delete devices
        if (role == null || !role.equals("ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        
        deviceService.delete(id);
        return ResponseEntity.ok().build();
    }
}
