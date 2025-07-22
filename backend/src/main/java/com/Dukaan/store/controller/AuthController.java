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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.UUID;

@RestController
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

    @Operation(summary = "Login and get JWT token", requestBody = @RequestBody(content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"email\": \"test@example.com\", \"password\": \"testpass\"}"))), responses = {
            @ApiResponse(responseCode = "200", description = "JWT token returned", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"token\": \"<jwt>\"}"))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or account locked", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Invalid credentials\"}")))
    }, description = "Account will be locked for 15 minutes after 5 failed login attempts.")
    @PostMapping("/login")
    public Map<String, String> login(@org.springframework.web.bind.annotation.RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");
        Map<String, String> response = new HashMap<>();
        User user = userRepository.findByEmail(email);
        if (user != null && user.getAccountLockedUntil() != null && user.getAccountLockedUntil().after(new Date())) {
            response.put("error", "Account is locked. Try again later.");
            return response;
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
            return response;
        } catch (Exception ex) {
            // Increment failed attempts
            if (user != null) {
                int attempts = user.getFailedLoginAttempts() + 1;
                user.setFailedLoginAttempts(attempts);
                if (attempts >= 5) {
                    user.setAccountLockedUntil(new Date(System.currentTimeMillis() + 15 * 60 * 1000)); // 15 min lock
                    user.setFailedLoginAttempts(0);
                }
                userRepository.save(user);
            }
            response.put("error", "Invalid credentials");
            return response;
        }
    }

    @Operation(summary = "Register a new user", requestBody = @RequestBody(content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"testpass\"}"))), responses = {
            @ApiResponse(responseCode = "200", description = "User registered", content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/register")
    public UserDTO register(@org.springframework.web.bind.annotation.RequestBody Map<String, String> userData) {
        User user = new User();
        user.setName(userData.get("username"));
        user.setEmail(userData.get("email"));
        user.setPassword(passwordEncoder.encode(userData.get("password")));
        user.setRole(userData.getOrDefault("role", "USER"));
        userRepository.save(user);
        UserDTO dto = new UserDTO();
        dto.setUsername(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }

    @Operation(summary = "Request password reset", requestBody = @RequestBody(content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"email\": \"test@example.com\"}"))), responses = {
            @ApiResponse(responseCode = "200", description = "Reset link sent if email exists", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"If the email exists, a reset link has been sent.\"}")))
    })
    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        User user = userRepository.findByEmail(email);
        Map<String, String> response = new HashMap<>();
        if (user == null) {
            response.put("message", "If the email exists, a reset link has been sent.");
            return response;
        }
        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(new Date(System.currentTimeMillis() + 1000 * 60 * 30)); // 30 min expiry
        userRepository.save(user);
        String resetLink = "http://localhost:8080/reset-password?email=" + email + "&token=" + token;
        String mailText = "To reset your password, click the following link (or copy and paste into your browser):\n" + resetLink;
        emailService.sendEmail(email, "Password Reset Request", mailText);
        response.put("message", "If the email exists, a reset link has been sent.");
        return response;
    }

    @Operation(summary = "Reset password using token", requestBody = @RequestBody(content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"email\": \"test@example.com\", \"token\": \"<resetToken>\", \"newPassword\": \"newpass\"}"))), responses = {
            @ApiResponse(responseCode = "200", description = "Password reset successful", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Password has been reset successfully\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Token expired\"}")))
    })
    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        User user = userRepository.findByEmail(email);
        Map<String, String> response = new HashMap<>();
        if (user == null || user.getPasswordResetToken() == null || user.getPasswordResetTokenExpiry() == null) {
            response.put("error", "Invalid password reset request");
            return response;
        }
        if (!user.getPasswordResetToken().equals(token)) {
            response.put("error", "Invalid token");
            return response;
        }
        if (user.getPasswordResetTokenExpiry().before(new Date())) {
            response.put("error", "Token expired");
            return response;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
        response.put("message", "Password has been reset successfully");
        return response;
    }
} 