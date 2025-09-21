package com.example.recycling_app.repository;

import com.example.recycling_app.domain.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(String uid);
}
