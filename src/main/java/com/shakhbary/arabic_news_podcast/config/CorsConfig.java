//package com.shakhbary.arabic_news_podcast.config;
//
//import java.util.Arrays;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
///**
// * CORS (Cross-Origin Resource Sharing) configuration. Allows the Angular frontend to communicate
// * with this backend API.
// */
//@Configuration
//public class CorsConfig {
//
//  @Value("${app.cors.allowed-origins}")
//  private String[] allowedOrigins;
//
//  @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
//  private String[] allowedMethods;
//
//  @Value("${app.cors.allowed-headers:*}")
//  private String[] allowedHeaders;
//
//  @Value("${app.cors.allow-credentials:true}")
//  private boolean allowCredentials;
//
//  @Value("${app.cors.max-age:3600}")
//  private long maxAge;
//
//  @Bean
//  public CorsConfigurationSource corsConfigurationSource() {
//    CorsConfiguration configuration = new CorsConfiguration();
//
//    // Allow specific origins (e.g., your Angular dev server)
//    configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
//
//    // Allow specific HTTP methods
//    configuration.setAllowedMethods(Arrays.asList(allowedMethods));
//
//    // Allow all headers (or specify specific ones)
//    configuration.setAllowedHeaders(Arrays.asList(allowedHeaders));
//
//    // Allow credentials (cookies, authorization headers)
//    configuration.setAllowCredentials(allowCredentials);
//
//    // Cache preflight response for 1 hour
//    configuration.setMaxAge(maxAge);
//
//    // Expose specific headers to frontend (if needed)
//    configuration.setExposedHeaders(
//        Arrays.asList("Authorization", "Content-Type", "X-Total-Count"));
//
//    // Apply CORS configuration to all endpoints
//    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//    source.registerCorsConfiguration("/**", configuration);
//
//    return source;
//  }
//}
