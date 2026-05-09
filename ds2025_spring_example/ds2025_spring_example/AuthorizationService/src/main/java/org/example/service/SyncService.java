package org.example.service;

import org.example.dto.SyncMessage;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SyncService {
    
    private static final Logger log = LoggerFactory.getLogger(SyncService.class);
    private final UserRepository userRepository;
    
    public SyncService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Transactional
    public void processSyncMessage(SyncMessage message) {
        log.info("Processing sync message: {}", message);
        
        if ("USER".equalsIgnoreCase(message.getType())) {
            handleUserSync(message);
        } else {
            log.debug("Ignoring non-USER sync message type: {}", message.getType());
        }
    }
    
    private void handleUserSync(SyncMessage message) {
        String action = message.getAction();
        
        if ("DELETE".equalsIgnoreCase(action)) {
            // Delete user by username
            userRepository.findByUsername(message.getUsername())
                .ifPresent(user -> {
                    userRepository.delete(user);
                    log.info("Deleted user from auth: {}", message.getUsername());
                });
        } else {
            log.debug("Ignoring {} action for auth service - users are created via register endpoint", action);
        }
    }
}
