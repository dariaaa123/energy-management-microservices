package com.example.demo.services;


import com.example.demo.dtos.UserDTO;
import com.example.demo.dtos.UserDetailsDTO;
import com.example.demo.dtos.builders.UserBuilder;
import com.example.demo.entities.User;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final SyncPublisher syncPublisher;

    @Value("${auth.service.url:http://authorization-service:8080}")
    private String authServiceUrl;

    @Autowired
    public UserService(UserRepository userRepository, SyncPublisher syncPublisher) {
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
        this.syncPublisher = syncPublisher;
    }

    public List<UserDTO> findUsers() {
        List<User> userList = userRepository.findAll();
        return userList.stream()
                .map(UserBuilder::toUserDTO)
                .collect(Collectors.toList());
    }

    public UserDetailsDTO findUserById(UUID id) {
        Optional<User> prosumerOptional = userRepository.findById(id);
        if (!prosumerOptional.isPresent()) {
            LOGGER.error("Person with id {} was not found in db", id);
            throw new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
        }
        return UserBuilder.toUserDetailsDTO(prosumerOptional.get());
    }

    public UUID insert(UserDetailsDTO personDTO) {
        User user = UserBuilder.toEntity(personDTO);
        user = userRepository.save(user);
        LOGGER.debug("Person with id {} was inserted in db", user.getId());
        
        // Sync with AuthorizationService
        if (personDTO.getUsername() != null && personDTO.getPassword() != null && personDTO.getRole() != null) {
            try {
                syncWithAuthService(personDTO.getUsername(), personDTO.getPassword(), personDTO.getRole());
                LOGGER.info("✅ User {} synced with AuthorizationService", personDTO.getUsername());
            } catch (Exception e) {
                LOGGER.error("❌ Failed to sync user with AuthorizationService: {}", e.getMessage());
                // Don't fail the user creation if auth sync fails
            }
        }
        
        // Publish sync message to RabbitMQ
        syncPublisher.publishUserSync("CREATE", user.getId().toString(), user.getUsername(), user.getName());
        
        return user.getId();
    }

    private void syncWithAuthService(String username, String password, String role) {
        try {
            String url = authServiceUrl + "/api/auth/register";
            
            Map<String, String> request = new HashMap<>();
            request.put("username", username);
            request.put("password", password);
            request.put("role", role);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
            
            restTemplate.postForEntity(url, entity, String.class);
            LOGGER.info("Successfully registered user {} in AuthorizationService", username);
        } catch (Exception e) {
            LOGGER.error("Failed to register user in AuthorizationService: {}", e.getMessage());
            // Don't throw - allow user creation to succeed even if auth sync fails
        }
    }

    public void update(UUID id, UserDetailsDTO userDTO) {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            LOGGER.error("User with id {} was not found in db", id);
            throw new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
        }
        
        User user = userOptional.get();
        user.setName(userDTO.getName());
        user.setAddress(userDTO.getAddress());
        user.setAge(userDTO.getAge());
        if (userDTO.getUsername() != null && !userDTO.getUsername().isEmpty()) {
            user.setUsername(userDTO.getUsername());
        }
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(userDTO.getPassword());
        }
        if (userDTO.getRole() != null && !userDTO.getRole().isEmpty()) {
            user.setRole(userDTO.getRole());
        }
        userRepository.save(user);
        LOGGER.debug("User with id {} was updated in db", id);
        
        // Publish sync message to RabbitMQ
        syncPublisher.publishUserSync("UPDATE", user.getId().toString(), user.getUsername(), user.getName());
    }

    public void delete(UUID id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            LOGGER.error("User with id {} was not found in db", id);
            throw new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
        }
        
        User user = userOptional.get();
        
        // Publish sync message to RabbitMQ before deletion
        syncPublisher.publishUserSync("DELETE", user.getId().toString(), user.getUsername(), user.getName());
        
        userRepository.deleteById(id);
        LOGGER.debug("User with id {} was deleted from db", id);
    }

}
