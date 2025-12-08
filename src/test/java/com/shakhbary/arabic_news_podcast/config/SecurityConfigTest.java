package com.shakhbary.arabic_news_podcast.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.shakhbary.arabic_news_podcast.models.Role;
import com.shakhbary.arabic_news_podcast.models.User;
import com.shakhbary.arabic_news_podcast.repositories.RoleRepository;
import com.shakhbary.arabic_news_podcast.repositories.UserRepository;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for SecurityConfig.
 *
 * Tests role-based access control and authentication:
 * - Public endpoints (no authentication required)
 * - Admin endpoints (401 for unauthenticated, 403 for non-admin)
 * - User endpoints (authentication required)
 * - Basic Authentication verification
 */
@SpringBootTest
@ActiveProfiles("test")  // Add this if using application-test.properties
@AutoConfigureMockMvc
@TestPropertySource(locations = "file:src/test/resources/application-test.properties")
@Transactional
@DisplayName("SecurityConfig Integration Tests")
class SecurityConfigTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private UserRepository userRepository;

    @Autowired private RoleRepository roleRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User regularUser;
    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Clean database
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create roles
        adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        roleRepository.save(adminRole);

        userRole = new Role();
        userRole.setName("ROLE_USER");
        roleRepository.save(userRole);

        // Create admin user
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        adminRoles.add(userRole);
        adminUser = createUser("admin", "admin@example.com", "admin123", adminRoles);
        userRepository.save(adminUser);

        // Create regular user
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(userRole);
        regularUser = createUser("user", "user@example.com", "user123", userRoles);
        userRepository.save(regularUser);
    }

    // ===== PUBLIC ENDPOINTS TESTS =====

    @Test
    @DisplayName("Should allow access to public endpoints without authentication")
    void testPublicEndpoints_NoAuth() throws Exception {
        // Registration endpoint
        mockMvc.perform(get("/api/auth/register")).andExpect(status().isMethodNotAllowed()); // GET not allowed, but accessible

        // Login endpoint
        mockMvc.perform(get("/api/auth/login")).andExpect(status().isMethodNotAllowed()); // GET not allowed, but accessible

        // Episodes endpoint
        mockMvc.perform(get("/api/episodes")).andExpect(status().isOk());

        // Home endpoint
        mockMvc.perform(get("/api/home/daily")).andExpect(status().isOk());
    }

    // ===== ADMIN ENDPOINTS TESTS =====

    @Test
    @DisplayName("Should return 401 for admin endpoints without authentication")
    void testAdminEndpoints_NoAuth() throws Exception {
        // Try to access admin automation endpoint
        mockMvc
                .perform(post("/api/admin/automation/process-episode").content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 403 for admin endpoints with non-admin user")
    void testAdminEndpoints_NonAdmin() throws Exception {
        // Regular user tries to access admin endpoint
        mockMvc
                .perform(
                        post("/api/admin/automation/process-episode")
                                .with(httpBasic("user", "user123"))
                                .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow access to admin endpoints with admin user")
    void testAdminEndpoints_AdminUser() throws Exception {
        // Admin user can access admin endpoint (may fail for other reasons, but not auth)
        ResultActions result = mockMvc
                .perform(
                        post("/api/admin/automation/process-episode")
                                .with(httpBasic("admin", "admin123"))
                                .content("{}"));

        int status = result.andReturn().getResponse().getStatus();
        assertThat(status).isNotIn(401, 403);
    }

    @Test
    @DisplayName("Should return 401 for admin endpoint with invalid credentials")
    void testAdminEndpoints_InvalidCredentials() throws Exception {
        mockMvc
                .perform(
                        post("/api/admin/automation/process-episode")
                                .with(httpBasic("admin", "wrongpassword"))
                                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ===== USER ENDPOINTS TESTS =====

    @Test
    @DisplayName("Should return 401 for user endpoints without authentication")
    void testUserEndpoints_NoAuth() throws Exception {
        mockMvc.perform(get("/api/users/" + regularUser.getId())).andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/progress/in-progress")).andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/ratings").content("{}")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should allow regular user to access user endpoints")
    void testUserEndpoints_RegularUser() throws Exception {
        // Regular user accessing their own data
        ResultActions result = mockMvc
                .perform(get("/api/users/" + regularUser.getId()).with(httpBasic("user", "user123")));

        int status = result.andReturn().getResponse().getStatus();
        assertThat(status).isNotIn(401, 403);
    }

    @Test
    @DisplayName("Should allow admin to access user endpoints")
    void testUserEndpoints_AdminUser() throws Exception {
        // Admin accessing user data
        ResultActions result = mockMvc
                .perform(get("/api/users/" + regularUser.getId()).with(httpBasic("admin", "admin123")));

        int status = result.andReturn().getResponse().getStatus();
        assertThat(status).isNotIn(401, 403);
    }

    // ===== BASIC AUTHENTICATION TESTS =====

    @Test
    @DisplayName("Should authenticate with valid Basic Auth credentials")
    void testBasicAuth_ValidCredentials() throws Exception {
        ResultActions result = mockMvc
                .perform(get("/api/users/" + regularUser.getId()).with(httpBasic("user", "user123")));

        int status = result.andReturn().getResponse().getStatus();
        assertThat(status).isNotEqualTo(401);
    }

    @Test
    @DisplayName("Should reject invalid username")
    void testBasicAuth_InvalidUsername() throws Exception {
        mockMvc
                .perform(
                        get("/api/users/" + regularUser.getId()).with(httpBasic("nonexistent", "user123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject invalid password")
    void testBasicAuth_InvalidPassword() throws Exception {
        mockMvc
                .perform(
                        get("/api/users/" + regularUser.getId()).with(httpBasic("user", "wrongpassword")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject empty credentials")
    void testBasicAuth_EmptyCredentials() throws Exception {
        mockMvc
                .perform(get("/api/users/" + regularUser.getId()).with(httpBasic("", "")))
                .andExpect(status().isUnauthorized());
    }

    // ===== ROLE-BASED ACCESS TESTS =====

    @Test
    @DisplayName("Should verify ADMIN role has access to both admin and user endpoints")
    void testRoles_AdminAccess() throws Exception {
        // Admin can access admin endpoints
        ResultActions result1 = mockMvc
                .perform(
                        post("/api/admin/automation/process-episode")
                                .with(httpBasic("admin", "admin123"))
                                .content("{}"));
        assertThat(result1.andReturn().getResponse().getStatus()).isNotEqualTo(403);

        // Admin can access user endpoints
        ResultActions result2 = mockMvc
                .perform(get("/api/users/" + adminUser.getId()).with(httpBasic("admin", "admin123")));
        assertThat(result2.andReturn().getResponse().getStatus()).isNotEqualTo(403);
    }

    @Test
    @DisplayName("Should verify USER role has no access to admin endpoints")
    void testRoles_UserAccessRestricted() throws Exception {
        // User can access user endpoints
        ResultActions result = mockMvc
                .perform(get("/api/users/" + regularUser.getId()).with(httpBasic("user", "user123")));
        assertThat(result.andReturn().getResponse().getStatus()).isNotEqualTo(403);

        // User cannot access admin endpoints
        mockMvc
                .perform(
                        post("/api/admin/automation/process-episode")
                                .with(httpBasic("user", "user123"))
                                .content("{}"))
                .andExpect(status().isForbidden());
    }

    // ===== SESSION MANAGEMENT TESTS =====

    @Test
    @DisplayName("Should be stateless - each request requires authentication")
    void testStatelessSession() throws Exception {
        // First request with auth - should succeed
        mockMvc
                .perform(get("/api/users/" + regularUser.getId()).with(httpBasic("user", "user123")))
                .andExpect(status().isOk());

        // Second request without auth - should fail (no session stored)
        mockMvc.perform(get("/api/users/" + regularUser.getId())).andExpect(status().isUnauthorized());
    }

    // ===== CORS TESTS =====

    @Test
    @DisplayName("Should handle CORS preflight requests")
    void testCors_PreflightRequest() throws Exception {
        mockMvc
                .perform(
                        options("/api/episodes")
                                .header("Origin", "http://localhost:4200")
                                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
    }

    // ===== EDGE CASES =====

    @Test
    @DisplayName("Should handle case-sensitive username")
    void testAuth_CaseSensitiveUsername() throws Exception {
        // Correct case
        mockMvc
                .perform(get("/api/users/" + regularUser.getId()).with(httpBasic("user", "user123")))
                .andExpect(status().isOk());

        // Wrong case - should fail
        mockMvc
                .perform(get("/api/users/" + regularUser.getId()).with(httpBasic("USER", "user123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should handle disabled user account")
    void testAuth_DisabledUser() throws Exception {
        // Disable user
        regularUser.setEnabled(false);
        userRepository.save(regularUser);

        // Try to authenticate
        mockMvc
                .perform(get("/api/users/" + regularUser.getId()).with(httpBasic("user", "user123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should allow multiple concurrent authentications")
    void testAuth_ConcurrentRequests() throws Exception {
        // Simulate multiple users accessing endpoints simultaneously
        mockMvc
                .perform(get("/api/users/" + regularUser.getId()).with(httpBasic("user", "user123")))
                .andExpect(status().isOk());

        mockMvc
                .perform(get("/api/users/" + adminUser.getId()).with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());

        // Both should work independently
        mockMvc
                .perform(get("/api/users/" + regularUser.getId()).with(httpBasic("user", "user123")))
                .andExpect(status().isOk());
    }

    // Helper method
    private User createUser(String username, String email, String password, Set<Role> roles) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setCreationDate(OffsetDateTime.now());
        user.setEnabled(true);
        user.setSecondsListened(0L);
        user.setRoles(roles);
        return user;
    }
}