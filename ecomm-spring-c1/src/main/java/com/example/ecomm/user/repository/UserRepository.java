package com.example.ecomm.user.repository;

import com.example.ecomm.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmailAndActiveTrue(String email);
    boolean existsByEmail(String email);
}
