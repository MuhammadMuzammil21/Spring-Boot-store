package com.Dukaan.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "User data transfer object")
public class UserDTO {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @Schema(description = "User's username", example = "johndoe", required = true)
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    @Schema(description = "User's email address", example = "john@example.com", required = true)
    private String email;
    
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(USER|ADMIN)$", message = "Role must be either USER or ADMIN")
    @Schema(description = "User's role", example = "USER", allowableValues = {"USER", "ADMIN"}, required = true)
    private String role;

    // Default constructor
    public UserDTO() {}

    // Constructor with all fields
    public UserDTO(String username, String email, String role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "UserDTO{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        UserDTO userDTO = (UserDTO) o;
        
        if (!username.equals(userDTO.username)) return false;
        return email.equals(userDTO.email);
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + email.hashCode();
        return result;
    }
}
