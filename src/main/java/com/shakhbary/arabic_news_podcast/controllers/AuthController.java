package com.shakhbary.arabic_news_podcast.controllers;

import com.shakhbary.arabic_news_podcast.dtos.LoginRequestDto;
import com.shakhbary.arabic_news_podcast.dtos.LoginResponseDto;
import com.shakhbary.arabic_news_podcast.dtos.UserDto;
import com.shakhbary.arabic_news_podcast.dtos.UserRegistrationRequestDto;
import com.shakhbary.arabic_news_podcast.models.User;
import com.shakhbary.arabic_news_podcast.repositories.UserRepository;
import com.shakhbary.arabic_news_podcast.services.UserService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for authentication operations. Handles user registration and related
 * authentication tasks.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  /**
   * Register a new user. This endpoint is public to allow new users to register. Assigns ROLE_USER
   * by default.
   *
   * @param registrationRequest User registration data
   * @return UserDto with full user information (excluding password)
   */
  @PostMapping("/register")
  public ResponseEntity<UserDto> registerUser(
      @RequestBody @Valid UserRegistrationRequestDto registrationRequest) {
    UserDto newUser = userService.registerNewUser(registrationRequest);
    return new ResponseEntity<>(newUser, HttpStatus.CREATED);
  }

  /**
   * Login endpoint - validates credentials and returns user info. Client should store credentials
   * and use Basic Auth for subsequent requests.
   *
   * @param loginRequest Login credentials (username and password)
   * @return LoginResponseDto containing user info and encoded credentials
   */
  @PostMapping("/login")
  public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequest) {
    // 1. Find user by username
    User user =
        userRepository
            .findByUsername(loginRequest.getUsername())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid username or password"));

    // 2. Verify password
    if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    // 3. Check if account is enabled
    if (!user.isEnabled()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is disabled");
    }

    // 4. Encode credentials for Basic Auth
    String credentials = loginRequest.getUsername() + ":" + loginRequest.getPassword();
    String encodedCredentials =
        Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

    // 5. Update last login date (optional but good practice)
    user.setLastLoginDate(OffsetDateTime.now());
    userRepository.save(user);

    // 6. Return user info + encoded credentials
    UserDto userDto =
        new UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getCreationDate(),
            user.getLastLoginDate());

    LoginResponseDto response =
        new LoginResponseDto(
            userDto, encodedCredentials, "Basic" // Auth type
            );

    return ResponseEntity.ok(response);
  }
}
