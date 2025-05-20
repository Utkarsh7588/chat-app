package com.example.chat_app.service;

import com.example.chat_app.model.UserPrincipal;
import com.example.chat_app.model.Users;
import com.example.chat_app.repository.UsersRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ChatUserDetailsService implements UserDetailsService {

    private UsersRepository repo;

    public ChatUserDetailsService(UsersRepository repo){
        this.repo=repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user= repo.findByEmail(username);
        if(user==null){
            throw  new UsernameNotFoundException("User not found with email: " + username);
        }
        return new UserPrincipal(user);
    }
}
