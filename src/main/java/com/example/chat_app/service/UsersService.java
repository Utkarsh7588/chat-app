package com.example.chat_app.service;

import com.example.chat_app.exceptions.UserAlreadyExistsException;
import com.example.chat_app.exceptions.UserNotFoundException;
import com.example.chat_app.exceptions.ValidationException;
import com.example.chat_app.model.Users;
import com.example.chat_app.repository.UsersRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class UsersService {

    private UsersRepository repo;
    private JWTService jwtService;
    private AuthenticationManager authManager;
    private ApplicationContext context;

    public UsersService(UsersRepository repo, JWTService jwtService, AuthenticationManager authManager, ApplicationContext context) {
        this.repo = repo;
        this.jwtService = jwtService;
        this.authManager = authManager;
        this.context = context;
    }

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    public Users register(Users user) {

        if (user == null) {
            throw new ValidationException("User cannot be null");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Email is required");
        }

        if (repo.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        user.setPassword(encoder.encode(user.getPassword()));
        return repo.save(user);
    }

    public Map<String, Object> verify(Users user) {
        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
        Map<String, Object> response = new HashMap<>();
        if (authentication.isAuthenticated()) {
            Users userData=repo.findByEmail(user.getEmail());
            response.put("name",userData.getName());
            response.put("age",userData.getAge());
            response.put("email",userData.getEmail());
            response.put("user_id",userData.getId());
            String token=jwtService.generateToken(user.getEmail());
            response.put("token",token);
            return response;
        }
        response.put("success",false);
        response.put("message","authentication failed");
        return response;
    }

    public Users getUserInfo(int id) {
        return repo.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
