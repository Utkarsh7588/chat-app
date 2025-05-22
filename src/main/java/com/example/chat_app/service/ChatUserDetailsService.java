package com.example.chat_app.service;

import com.example.chat_app.helper.Validate;
import com.example.chat_app.model.UserPrincipal;
import com.example.chat_app.model.Users;
import com.example.chat_app.repository.UsersRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class ChatUserDetailsService implements UserDetailsService {

    private UsersRepository repo;

    public ChatUserDetailsService(UsersRepository repo){
        this.repo=repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("Username/Email cannot be empty");
        }
        Users user;
        if(Validate.validateEmail(username)){
          user= repo.findByEmail(username);
        }
        else{
            user=repo.findByUsername(username);
        }
        if(user==null){
            throw  new UsernameNotFoundException("User not found with email: " + username);
        }
        return new UserPrincipal(user);
    }
}
