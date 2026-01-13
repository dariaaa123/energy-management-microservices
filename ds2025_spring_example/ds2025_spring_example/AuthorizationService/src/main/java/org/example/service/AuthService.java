package org.example.service;

import org.example.dto.AuthResponse;
import org.example.dto.LoginRequest;
import org.example.dto.RegisterRequest;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;


    public AuthService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }
        
        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );
        userRepository.save(user);
        String token = jwtService.generateToken(user.getUsername(), String.valueOf(user.getRole()));
        return new AuthResponse(token, user.getUsername(), String.valueOf(user.getRole()), user.getId());
    }

    public AuthResponse login(LoginRequest request) {
        System.out.println("🔑 Login attempt for: " + request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("❌ User not found: " + request.getUsername()));

        System.out.println("✅ Found user: " + user.getUsername());
        System.out.println("Raw password: " + request.getPassword());
        System.out.println("Stored hash: " + user.getPassword());
        System.out.println("Match result: " + passwordEncoder.matches(request.getPassword(), user.getPassword()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("❌ Invalid credentials");
        }

        String token = jwtService.generateToken(user.getUsername(), String.valueOf(user.getRole()));
        System.out.println("✅ Token generated: " + token);
        System.out.println("✅ User ID: " + user.getId());
        return new AuthResponse(token, user.getUsername(), String.valueOf(user.getRole()), user.getId());
    }


    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }
}
