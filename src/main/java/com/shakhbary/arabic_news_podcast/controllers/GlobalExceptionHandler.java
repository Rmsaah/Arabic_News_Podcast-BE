package com.shakhbary.arabic_news_podcast.controllers;

import com.shakhbary.arabic_news_podcast.exceptions.BadRequestException;
import com.shakhbary.arabic_news_podcast.exceptions.DuplicateResourceException;
import com.shakhbary.arabic_news_podcast.exceptions.ResourceNotFoundException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Global exception handler for all REST controllers. Provides consistent error responses across the
 * application.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Standard API error response.
   *
   * @param error Error type/category
   * @param message Human-readable error message
   * @param timestamp When the error occurred
   */
  record ApiError(String error, String message, OffsetDateTime timestamp) {
    static ApiError of(String error, String message) {
      return new ApiError(error, message, OffsetDateTime.now());
    }
  }

  /**
   * Validation error response with field-specific errors.
   *
   * @param error Error type
   * @param message Overall error message
   * @param timestamp When the error occurred
   * @param fieldErrors Map of field names to error messages
   */
  record ValidationError(
      String error, String message, OffsetDateTime timestamp, Map<String, String> fieldErrors) {
    static ValidationError of(String message, Map<String, String> fieldErrors) {
      return new ValidationError("VALIDATION_ERROR", message, OffsetDateTime.now(), fieldErrors);
    }
  }

  /** Handle ResourceNotFoundException (404 Not Found). */
  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ApiError handleNotFound(ResourceNotFoundException ex) {
    log.warn("Resource not found: {}", ex.getMessage());
    return ApiError.of("NOT_FOUND", ex.getMessage());
  }

  /** Handle BadRequestException (400 Bad Request). */
  @ExceptionHandler(BadRequestException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleBadRequest(BadRequestException ex) {
    log.warn("Bad request: {}", ex.getMessage());
    return ApiError.of("BAD_REQUEST", ex.getMessage());
  }

  /** Handle IllegalArgumentException (400 Bad Request). */
  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("Illegal argument: {}", ex.getMessage());
    return ApiError.of("BAD_REQUEST", ex.getMessage());
  }

  /**
   * Handle validation errors from @Valid annotations (400 Bad Request). Returns detailed
   * field-level validation errors.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ValidationError handleValidationErrors(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();

    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });

    log.warn("Validation failed: {} field(s) with errors", errors.size());
    return ValidationError.of("Validation failed", errors);
  }

  /** Handle DuplicateResourceException (409 Conflict). */
  @ExceptionHandler(DuplicateResourceException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiError handleDuplicateResource(DuplicateResourceException ex) {
    log.warn("Duplicate resource: {}", ex.getMessage());
    return ApiError.of("CONFLICT", ex.getMessage());
  }

  /**
   * Handle ResponseStatusException (for authorization failures, etc.). Returns appropriate status
   * code and error message.
   */
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex) {
    HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
    String errorType = status != null ? status.name() : "ERROR";

    log.warn("{}: {}", errorType, ex.getReason());
    ApiError error =
        ApiError.of(errorType, ex.getReason() != null ? ex.getReason() : "An error occurred");
    return new ResponseEntity<>(error, status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Handle all other unexpected exceptions (500 Internal Server Error). Logs the full exception for
   * debugging while returning a safe message to clients.
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiError handleGeneric(Exception ex) {
    log.error("Unexpected error occurred", ex);
    return ApiError.of("INTERNAL_ERROR", "An unexpected error occurred. Please try again later.");
  }
}
