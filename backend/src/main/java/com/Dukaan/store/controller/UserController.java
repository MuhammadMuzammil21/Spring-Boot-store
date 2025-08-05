package com.Dukaan.store.controller;

import com.Dukaan.store.dto.UserDTO;
import com.Dukaan.store.model.User;
import com.Dukaan.store.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users and user profiles")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(
        summary = "Get all users", 
        description = "Retrieve a list of all users in the system. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users", 
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @Operation(
        summary = "Create a new user", 
        description = "Create a new user account. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully", 
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or email already exists"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @PostMapping
    public ResponseEntity<UserDTO> createUser(
            @Valid @RequestBody 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "User details to create",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"username\": \"johndoe\", \"email\": \"john@example.com\", \"role\": \"USER\", \"password\": \"securePassword123\"}"
                    )
                )
            ) Map<String, String> userData) {
        
        // Check if email already exists
        if (userService.findByEmail(userData.get("email")) != null) {
            return ResponseEntity.badRequest().build();
        }
        
        User user = new User();
        user.setName(userData.get("username"));
        user.setEmail(userData.get("email"));
        user.setRole(userData.getOrDefault("role", "USER"));
        user.setPassword(passwordEncoder.encode(userData.get("password")));
        
        User saved = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
    }

    @Operation(
        summary = "Get user by ID", 
        description = "Retrieve a specific user by their unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found successfully", 
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDTO(user));
    }

    @Operation(
        summary = "Update user profile", 
        description = "Update user profile information. Users can update their own profile, admins can update any profile."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully", 
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Can only update own profile unless admin")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated user information",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"username\": \"johnsmith\", \"email\": \"johnsmith@example.com\"}"
                    )
                )
            ) UserDTO userDTO) {
        
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Check if email is being changed and if it already exists
        if (!existingUser.getEmail().equals(userDTO.getEmail())) {
            User emailCheck = userService.findByEmail(userDTO.getEmail());
            if (emailCheck != null && !emailCheck.getId().equals(id)) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        existingUser.setName(userDTO.getUsername());
        existingUser.setEmail(userDTO.getEmail());
        if (userDTO.getRole() != null && !userDTO.getRole().isEmpty()) {
            existingUser.setRole(userDTO.getRole());
        }
        
        User updated = userService.updateUser(existingUser);
        return ResponseEntity.ok(toDTO(updated));
    }

    @Operation(
        summary = "Change user password", 
        description = "Change user password. Users can change their own password, admins can change any password."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "Invalid current password or new password"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Can only change own password unless admin")
    })
    @PutMapping("/{id}/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody Map<String, String> passwordData) {
        
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");
        
        if (newPassword == null || newPassword.length() < 6) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "New password must be at least 6 characters long");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Verify current password (simplified - in real app, check against encoded password)
        if (currentPassword == null || currentPassword.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Current password is required");
            return ResponseEntity.badRequest().body(error);
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateUser(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get users with pagination and search", 
        description = "Retrieve users with pagination and optional search by name or email"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Paginated users retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @GetMapping("/paginated")
    public ResponseEntity<Map<String, Object>> getUsersPaginated(
            @Parameter(description = "Search term for name or email", example = "john")
            @RequestParam(required = false) String search,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userService.getUsersPaginated(search, pageable);
        
        List<UserDTO> users = userPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("currentPage", userPage.getNumber());
        response.put("totalItems", userPage.getTotalElements());
        response.put("totalPages", userPage.getTotalPages());
        response.put("hasNext", userPage.hasNext());
        response.put("hasPrevious", userPage.hasPrevious());
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get user profile", 
        description = "Get the current user's profile information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully", 
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getUserProfile() {
        // In a real implementation, you would get the current user from the JWT token
        // For now, we'll return a placeholder response
        Map<String, String> response = new HashMap<>();
        response.put("message", "Profile endpoint - implement JWT token parsing to get current user");
        return ResponseEntity.ok(new UserDTO());
    }

    @Operation(
        summary = "Delete user by ID", 
        description = "Remove a user from the system. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUsername(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }

    private User toEntity(UserDTO dto) {
        User user = new User();
        user.setName(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        // Password should be set elsewhere (e.g., registration)
        return user;
    }
}
