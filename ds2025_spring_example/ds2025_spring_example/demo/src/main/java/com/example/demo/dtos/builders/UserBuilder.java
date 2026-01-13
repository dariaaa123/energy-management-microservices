package com.example.demo.dtos.builders;

import com.example.demo.dtos.UserDTO;
import com.example.demo.dtos.UserDetailsDTO;
import com.example.demo.entities.User;

public class UserBuilder {

    private UserBuilder() {
    }

    public static UserDTO toUserDTO(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getRole(), user.getName(), user.getAddress(), user.getAge());
    }

    public static UserDetailsDTO toUserDetailsDTO(User user) {
        UserDetailsDTO dto = new UserDetailsDTO(user.getId(), user.getUsername(), user.getRole(), user.getName(), user.getAddress(), user.getAge());
        // Don't include password in the response
        return dto;
    }

    public static User toEntity(UserDetailsDTO userDetailsDTO) {
        User user = new User(userDetailsDTO.getName(),
                userDetailsDTO.getAddress(),
                userDetailsDTO.getAge());
        user.setUsername(userDetailsDTO.getUsername());
        user.setPassword(userDetailsDTO.getPassword());
        user.setRole(userDetailsDTO.getRole());
        return user;
    }
}
