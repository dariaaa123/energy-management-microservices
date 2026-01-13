package com.example.demo.dtos;

import java.util.Objects;
import java.util.UUID;

public class UserDTO {
    private UUID id;
    private String username;
    private String role;
    private String name;
    private String address;
    private int age;

    public UserDTO() {}
    public UserDTO(UUID id, String username, String role, String name, String address, int age) {
        this.id = id; 
        this.username = username;
        this.role = role;
        this.name = name; 
        this.address = address;
        this.age = age;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO that = (UserDTO) o;
        return age == that.age && Objects.equals(name, that.name) && Objects.equals(address, that.address);
    }
    @Override public int hashCode() { return Objects.hash(name, address, age); }
}
