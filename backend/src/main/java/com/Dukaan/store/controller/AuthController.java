package com.Dukaan.store.controller;

import com.Dukaan.store.dto.UserDTO;
import com.Dukaan.store.model.User;
import com.Dukaan.store.repository.UserRepository;
import com.Dukaan.store.security.JwtUtil;
import com.Dukaan.store.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.UUID;

@RestController
@Tag(name = "Authentication", description = "APIs for user authentication and account management")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private EmailService emailService;

    @Operation(
        summary = "User login", 
        description = "Authenticate user and return JWT token. Account will be locked for 15 minutes after 5 failed login attempts."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful - JWT token returned", 
                    content = @Content(mediaType = "application/json", 
                    examples = @ExampleObject(value = "{\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"user\": {\"username\": \"johndoe\", \"email\": \"john@example.com\", \"role\": \"USER\"}}"))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", 
                    content = @Content(mediaType = "application/json", 
                    examples = @ExampleObject(value = "{\"error\": \"Invalid credentials\"}"))),
        @ApiResponse(responseCode = "423", description = "Account locked due to multiple failed attempts", 
                    content = @Content(mediaType = "application/json", 
                    examples = @ExampleObject(value = "{\"error\": \"Account is locked. Try again later.\"}"))),
        @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @org.springframework.web.bind.annotation.RequestBody 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "User login credentials",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"email\": \"john@example.com\", \"password\": \"securePassword123\"}"
                    )
                )
            ) Map<String, String> loginData) {
        
        String email = loginData.get("email");
        String password = loginData.get("password");
        Map<String, Object> response = new HashMap<>();
        
        if (email == null || password == null) {
            response.put("error", "Email and password are required");
            return ResponseEntity.badRequest().body(response);
        }
        
        User user = userRepository.findByEmail(email);
        
        // Check if account is locked
        if (user != null && user.getAccountLockedUntil() != null && user.getAccountLockedUntil().after(new Date())) {
            response.put("error", "Account is locked. Try again later.");
            return ResponseEntity.status(HttpStatus.LOCKED).body(response);
        }
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);
            
            // Reset failed attempts on success
            if (user != null) {
                user.setFailedLoginAttempts(0);
                user.setAccountLockedUntil(null);
                userRepository.save(user);
            }
            
            response.put("token", token);
            response.put("user", toUserDTO(user));
            response.put("message", "Login successful");
            
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            // Increment failed attempts
            if (user != null) {
                int attempts = user.getFailedLoginAttempts() + 1;
                user.setFailedLoginAttempts(attempts);
                if (attempts >= 5) {
                    user.setAccountLockedUntil(new Date(System.currentTimeMillis() + 15 * 60 * 1000)); // 15 min lock
                    user.setFailedLoginAttempts(0);
                    response.put("error", "Account locked due to multiple failed attempts. Try again in 15 minutes.");
                    userRepository.save(user);
                    return ResponseEntity.status(HttpStatus.LOCKED).body(response);
                }
                userRepository.save(user);
            }
            
            response.put("error", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @Operation(
        summary = "User registration", 
        description = "Register a new user account in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully", 
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or email already exists"),
        @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @PostMapping("/register")
    public ResponseEntity<Object> register(
            @Valid @org.springframework.web.bind.annotation.RequestBody 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "User registration details",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"username\": \"johndoe\", \"email\": \"john@example.com\", \"password\": \"securePassword123\"}"
                    )
                )
            ) Map<String, String> userData) {
        
        String email = userData.get("email");
        String username = userData.get("username");
        String password = userData.get("password");
        
        // Validation
        if (email == null || username == null || password == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Username, email, and password are required");
            return ResponseEntity.badRequest().body(error);
        }
        
        if (password.length() < 6) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Password must be at least 6 characters long");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(email) != null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Email already registered");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
        
        User user = new User();
        user.setName(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(userData.getOrDefault("role", "USER"));
        user.setFailedLoginAttempts(0);
        
        User savedUser = userRepository.save(user);
        
        UserDTO dto = toUserDTO(savedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(
        summary = "Request password reset", 
        description = "Send password reset link to user's email address"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reset link sent if email exists", 
                    content = @Content(mediaType = "application/json", 
                    examples = @ExampleObject(value = "{\"message\": \"If the email exists, a reset link has been sent.\"}"))),
        @ApiResponse(responseCode = "400", description = "Invalid email format")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Email address for password reset",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"email\": \"john@example.com\"}"
                    )
                )
            ) Map<String, String> body) {
        
        String email = body.get("email");
        Map<String, String> response = new HashMap<>();
        
        if (email == null || email.trim().isEmpty()) {
            response.put("error", "Email is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        User user = userRepository.findByEmail(email);
        
        // Always return the same message for security (don't reveal if email exists)
        response.put("message", "If the email exists, a reset link has been sent.");
        
        if (user != null) {
            String token = UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            user.setPasswordResetTokenExpiry(new Date(System.currentTimeMillis() + 1000 * 60 * 30)); // 30 min expiry
            userRepository.save(user);
            
            String resetLink = "http://localhost:8080/reset-password?email=" + email + "&token=" + token;
            String mailText = "To reset your password, click the following link (or copy and paste into your browser):\n\n" + 
                             resetLink + "\n\nThis link will expire in 30 minutes.\n\n" +
                             "If you did not request this password reset, please ignore this email.";
            
            try {
                emailService.sendEmail(email, "Password Reset Request", mailText);
            } catch (Exception e) {
                // Log error but don't reveal to user
                System.err.println("Failed to send email: " + e.getMessage());
            }
        }
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Reset password using token", 
        description = "Reset user password using the token received via email"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successful", 
                    content = @Content(mediaType = "application/json", 
                    examples = @ExampleObject(value = "{\"message\": \"Password has been reset successfully\"}"))),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token"),
        @ApiResponse(responseCode = "404", description = "Invalid reset request")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Password reset details",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"email\": \"john@example.com\", \"token\": \"uuid-token-here\", \"newPassword\": \"newSecurePassword123\"}"
                    )
                )
            ) Map<String, String> body) {
        
        String email = body.get("email");
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        Map<String, String> response = new HashMap<>();
        
        // Validation
        if (email == null || token == null || newPassword == null) {
            response.put("error", "Email, token, and new password are required");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (newPassword.length() < 6) {
            response.put("error", "New password must be at least 6 characters long");
            return ResponseEntity.badRequest().body(response);
        }
        
        User user = userRepository.findByEmail(email);
        
        if (user == null || user.getPasswordResetToken() == null || user.getPasswordResetTokenExpiry() == null) {
            response.put("error", "Invalid password reset request");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        if (!user.getPasswordResetToken().equals(token)) {
            response.put("error", "Invalid token");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (user.getPasswordResetTokenExpiry().before(new Date())) {
            response.put("error", "Token expired");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Reset password and clear reset token
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        user.setFailedLoginAttempts(0); // Reset failed attempts
        user.setAccountLockedUntil(null); // Unlock account if locked
        
        userRepository.save(user);
        
        response.put("message", "Password has been reset successfully");
        return ResponseEntity.ok(response);
    }

    private UserDTO toUserDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setUsername(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }
}
