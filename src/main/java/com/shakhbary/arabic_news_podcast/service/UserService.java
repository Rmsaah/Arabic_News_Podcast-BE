package com.shakhbary.arabic_news_podcast.service;


import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shakhbary.arabic_news_podcast.dtos.UserRegistrationRequest;
import com.shakhbary.arabic_news_podcast.models.Role;
import com.shakhbary.arabic_news_podcast.models.User;
import com.shakhbary.arabic_news_podcast.repositories.RoleRepository;
import com.shakhbary.arabic_news_podcast.repositories.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository; // Need to create this repository

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerNewUser(UserRegistrationRequest registrationRequest) {
        
        // 1. Check if username/email already exists
        if (userRepository.existsByUsername(registrationRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        // 2. Find the default 'USER' role
        // NOTE: The role name MUST match what is in your database ('ROLE_USER')
        Optional<Role> userRoleOpt = roleRepository.findByName("ROLE_USER");
        if (userRoleOpt.isEmpty()) {
            throw new RuntimeException("Error: Role is not found in the database.");
        }
        Role userRole = userRoleOpt.get();

        // 3. Create and populate the User entity
        User user = new User();
        user.setUsername(registrationRequest.getUsername());
        user.setEmail(registrationRequest.getEmail());
        user.setFirstName(registrationRequest.getFirstName());
        user.setLastName(registrationRequest.getLastName());
        
        // **IMPORTANT: Encode the password before saving!**
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        
        // Set other default fields
        user.setCreatedAt(OffsetDateTime.now());
        user.setSecondsListened(0L);
        user.setEnabled(true);

        // 4. Assign the role
        user.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        // 5. Save the user
        return userRepository.save(user);
    }
}
