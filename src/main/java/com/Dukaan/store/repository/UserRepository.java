package com.Dukaan.store.repository;

import com.Dukaan.store.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email); // For login and auth
}
