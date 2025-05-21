package com.example.chat_app.service;

import com.example.chat_app.exceptions.UserNotFoundException;
import com.example.chat_app.model.Users;
import com.example.chat_app.repository.UsersRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class JWTService {

    private UsersRepository usersRepository;

    public JWTService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    private String secretKey = "1njOWYLlRfEJgrPr0CXWLL7B+yYtuS09nnxhL6rgZms=";

    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        Users user = usersRepository.findByEmail(email);
        claims.put("id", user.getId());
        claims.put("name", user.getName());
        claims.put("email", user.getEmail());

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 30)))
                .and()
                .signWith(getSecretKey())
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser() // create parser
                .verifyWith(getSecretKey()) // attach signature key
                .build()
                .parseSignedClaims(token) // parse the token
                .getPayload(); // extract payload (body/claims)
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject(); // subject = username
    }

    public int extractId(String token) {
        return extractAllClaims(token).get("id", Integer.class);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration(); // get expiry date
        return expiration.before(new Date()); // check if expired
    }

    public boolean validateToken(String token) {
        try{
            if (isTokenExpired(token)) {
                return false;
            }
            int userId = extractId(token);
            if(usersRepository.existsById(userId)){
                Users user = usersRepository.findById(userId).orElse(new Users());
                return Objects.equals(user.getEmail(), extractEmail(token));
            }
            return false;
        }
        catch (Exception e){
            return false;
        }
    }
}
