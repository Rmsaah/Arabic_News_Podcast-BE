package com.shakhbary.arabic_news_podcast.services.Impl;

import com.shakhbary.arabic_news_podcast.dtos.UserDto;
import com.shakhbary.arabic_news_podcast.dtos.UserRegistrationRequestDto;
import com.shakhbary.arabic_news_podcast.exceptions.DuplicateResourceException;
import com.shakhbary.arabic_news_podcast.exceptions.ResourceNotFoundException;
import com.shakhbary.arabic_news_podcast.models.Role;
import com.shakhbary.arabic_news_podcast.models.User;
import com.shakhbary.arabic_news_podcast.repositories.RoleRepository;
import com.shakhbary.arabic_news_podcast.repositories.UserRepository;
import com.shakhbary.arabic_news_podcast.services.UserService;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public UserDto registerNewUser(UserRegistrationRequestDto registrationRequest) {
    // Check if username already exists
    if (userRepository.existsByUsername(registrationRequest.getUsername())) {
      throw new DuplicateResourceException("Username is already taken");
    }

    // Check if email already exists
    if (userRepository.existsByEmail(registrationRequest.getEmail())) {
      throw new DuplicateResourceException("Email is already registered");
    }

    // Create new user
    User user = new User();
    user.setUsername(registrationRequest.getUsername());
    user.setEmail(registrationRequest.getEmail());
    user.setPassword(passwordEncoder.encode(registrationRequest.getPassword())); // Hash password
    user.setFirstName(registrationRequest.getFirstName());
    user.setLastName(registrationRequest.getLastName());
    user.setCreationDate(OffsetDateTime.now());
    user.setEnabled(true);
    user.setSecondsListened(0L);

    // Assign default ROLE_USER
    Role userRole =
        roleRepository
            .findByName("ROLE_USER")
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Default role ROLE_USER not found. Please run database initialization."));

    Set<Role> roles = new HashSet<>();
    roles.add(userRole);
    user.setRoles(roles);

    // Save user
    user = userRepository.save(user);

    // Return as DTO (no password exposed)
    return new UserDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getCreationDate(),
        user.getLastLoginDate());
  }

  @Override
  @Transactional(readOnly = true)
  public UserDto getUser(UUID userId, String requestingUsername) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    // Authorization check: user can only view their own profile unless they're an admin
    validateUserAccess(user.getUsername(), requestingUsername, "view this profile");

    return new UserDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getCreationDate(),
        user.getLastLoginDate());
  }

  @Override
  @Transactional
  public UserDto updateUserName(
      UUID userId, String firstName, String lastName, String requestingUsername) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    // Authorization check: user can only update their own profile unless they're an admin
    validateUserAccess(user.getUsername(), requestingUsername, "update this profile");

    user.setFirstName(firstName);
    user.setLastName(lastName);
    user = userRepository.save(user);

    return new UserDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getCreationDate(),
        user.getLastLoginDate());
  }

  /**
   * Validates that the requesting user has permission to access the target user's data. Allows
   * access if: requesting user is the target user OR requesting user has ADMIN role.
   *
   * @param targetUsername Username of the user being accessed
   * @param requestingUsername Username of the user making the request
   * @param action Description of the action being attempted (for error message)
   * @throws ResponseStatusException if access is denied
   */
  private void validateUserAccess(String targetUsername, String requestingUsername, String action) {
    // Check if requesting user is trying to access their own data
    if (targetUsername.equals(requestingUsername)) {
      return; // Access granted
    }

    // Check if requesting user has ADMIN role
    boolean isAdmin =
        SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

    if (!isAdmin) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only " + action);
    }
  }
}
