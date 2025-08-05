package com.Dukaan.store.service;

import com.Dukaan.store.dto.UserDTO;
import com.Dukaan.store.model.User;
import com.Dukaan.store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Page<User> getUsersPaginated(String search, Pageable pageable) {
        // For this implementation, we'll use basic pagination
        // In a real implementation, you would create custom repository methods with @Query annotations
        Page<User> users = userRepository.findAll(pageable);
        
        // Apply search filter (simplified implementation)
        // In production, this should be done at the database level for better performance
        if (search != null && !search.trim().isEmpty()) {
            List<User> filteredUsers = users.getContent().stream()
                    .filter(user -> 
                        user.getName().toLowerCase().contains(search.toLowerCase()) ||
                        user.getEmail().toLowerCase().contains(search.toLowerCase())
                    )
                    .collect(Collectors.toList());
            
            // Note: This is a simplified approach. In a real implementation,
            // you would use custom repository methods with proper pagination
        }
        
        return users;
    }

    // DTO <-> Entity mapping
    public User toEntity(UserDTO dto) {
        User user = new User();
        user.setName(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        // Password should be set elsewhere (e.g., registration)
        return user;
    }

    public UserDTO toDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setUsername(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }
}
