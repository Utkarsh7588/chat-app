package com.example.chat_app.repository;

import com.example.chat_app.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UsersRepository extends JpaRepository<Users,Integer> {
    boolean existsByEmail(String email);

    Users findByEmail(String email);
    @Query("SELECT u.username FROM Users u WHERE u.id = :id")
    String findUsernameById(Integer id);

    Users findByUsername(String username);

    boolean existsByUsername(String username);
}

