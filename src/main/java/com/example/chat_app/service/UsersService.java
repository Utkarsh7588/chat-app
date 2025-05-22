package com.example.chat_app.service;

import com.example.chat_app.exceptions.*;
import com.example.chat_app.model.Users;
import com.example.chat_app.repository.UsersRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
            throw new EmailAlreadyExistsException("Email already registered");
        }
        if(repo.existsByUsername(user.getUsername())){
            throw new UsernameAlreadyExistsException("Username already taken");
        }

        user.setPassword(encoder.encode(user.getPassword()));
        return repo.save(user);
    }

    public Map<String, Object> verify(Users user) {
        Authentication authentication;
        if(user.getEmail()!=null){
            authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

        }
        else if(user.getUsername()!=null){
            authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        }
        else{
            throw new EmptyCredentialsException("Credentials were empty");
        }
        Map<String, Object> response = new HashMap<>();
        if (authentication.isAuthenticated()) {
            Users userData;
            if(user.getEmail()==null){
                userData=repo.findByUsername(user.getUsername());
                System.out.println(userData);
            }
            else{
                userData=repo.findByEmail(user.getEmail());
            }
            if(userData==null){
                throw  new UsernameNotFoundException("User not found");
            }
            response.put("name",userData.getName());
            response.put("age",userData.getAge());
            response.put("email",userData.getEmail());
            response.put("username",userData.getUsername());
            response.put("user_id",userData.getId());
            String token=jwtService.generateToken(userData.getUsername());
            response.put("token",token);
            return response;
        }

        throw new BadCredentialsException("Incorrect login details");
    }

    public Users getUserInfo(int id) {
        return repo.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
