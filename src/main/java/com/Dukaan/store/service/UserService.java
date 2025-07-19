package com.Dukaan.store.service;

import com.Dukaan.store.model.User;
import com.Dukaan.store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository UserRepository;

    public List<User> getAllUsers() {
        return UserRepository.findAll();
    }

    public User getUserById(Long id) {
        return UserRepository.findById(id).orElse(null);
    }

    public User createUser(User User) {
        return UserRepository.save(User);
    }

    public void deleteUser(Long id) {
        UserRepository.deleteById(id);
    }
}