package com.example.chat_app.controller;

import com.example.chat_app.model.Users;
import com.example.chat_app.service.JWTService;
import com.example.chat_app.service.UsersService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UsersController {
    private final UsersService service;
    private  final JWTService jwtService;
    public UsersController(UsersService service,JWTService jwtService){
        this.service=service;
        this.jwtService=jwtService;
    }

    @PostMapping("/register")
    public Users register(@RequestBody Users user){
        return service.register(user);
    }

    @PostMapping("/login")
    public Map<String,Object> login(@RequestBody Users user){
        return service.verify(user);
    }

    @GetMapping("/user-info")
    public Users getUserInfo(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);
        int id=jwtService.extractId(token);
        return service.getUserInfo(id);
    }
}
