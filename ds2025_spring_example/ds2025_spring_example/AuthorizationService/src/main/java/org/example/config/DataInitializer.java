package org.example.config;

import org.example.model.Role;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if users already exist to avoid duplicates
        if (userRepository.count() == 0) {
            System.out.println("🚀 Initializing default users...");
            
            // Create admin user
            User admin = new User(
                "admin", 
                passwordEncoder.encode("admin123"), 
                Role.ADMIN
            );
            userRepository.save(admin);
            System.out.println("✅ Created admin user: admin/admin123");
            
            // Create client user
            User client = new User(
                "client", 
                passwordEncoder.encode("client123"), 
                Role.CLIENT
            );
            userRepository.save(client);
            System.out.println("✅ Created client user: client/client123");
            
            // Create test user
            User testUser = new User(
                "test", 
                passwordEncoder.encode("password"), 
                Role.CLIENT
            );
            userRepository.save(testUser);
            System.out.println("✅ Created test user: test/password");
            
            System.out.println("🎉 Default users initialized successfully!");
        } else {
            System.out.println("ℹ️ Users already exist, skipping initialization");
        }
    }
}