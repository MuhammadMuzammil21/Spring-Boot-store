package com.Dukaan.store.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        logger.warn("Validation error: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "Validation failed", 
            HttpStatus.BAD_REQUEST.value(),
            request.getDescription(false)
        );
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        
        response.put("fieldErrors", fieldErrors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle constraint violation exceptions
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        
        logger.warn("Constraint violation: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "Constraint violation", 
            HttpStatus.BAD_REQUEST.value(),
            request.getDescription(false)
        );
        
        Map<String, String> violations = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
            violations.put(violation.getPropertyPath().toString(), violation.getMessage())
        );
        
        response.put("violations", violations);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle authentication exceptions
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        
        logger.warn("Authentication error: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "Authentication failed", 
            HttpStatus.UNAUTHORIZED.value(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle bad credentials specifically
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        
        logger.warn("Bad credentials: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "Invalid credentials", 
            HttpStatus.UNAUTHORIZED.value(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        logger.warn("Access denied: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "Access denied - Insufficient permissions", 
            HttpStatus.FORBIDDEN.value(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle entity not found exceptions
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFoundException(
            EntityNotFoundException ex, WebRequest request) {
        
        logger.warn("Entity not found: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "Resource not found", 
            HttpStatus.NOT_FOUND.value(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle data integrity violations (e.g., unique constraint violations)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, WebRequest request) {
        
        logger.warn("Data integrity violation: {}", ex.getMessage());
        
        String message = "Data integrity violation";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("email")) {
                message = "Email already exists";
            } else if (ex.getMessage().contains("unique")) {
                message = "Duplicate entry - resource already exists";
            }
        }
        
        Map<String, Object> response = createErrorResponse(
            message, 
            HttpStatus.CONFLICT.value(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Handle HTTP message not readable (malformed JSON)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {
        
        logger.warn("Malformed JSON request: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "Malformed JSON request", 
            HttpStatus.BAD_REQUEST.value(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle missing request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, WebRequest request) {
        
        logger.warn("Missing request parameter: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "Missing required parameter: " + ex.getParameterName(), 
            HttpStatus.BAD_REQUEST.value(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle method argument type mismatch
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        
        logger.warn("Method argument type mismatch: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "Invalid parameter type for: " + ex.getName(), 
            HttpStatus.BAD_REQUEST.value(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle HTTP method not supported
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        
        logger.warn("HTTP method not supported: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "HTTP method not supported: " + ex.getMethod(), 
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handle no handler found (404 errors)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandlerFoundException(
            NoHandlerFoundException ex, WebRequest request) {
        
        logger.warn("No handler found: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "Endpoint not found: " + ex.getRequestURL(), 
            HttpStatus.NOT_FOUND.value(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle custom business logic exceptions
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {
        
        logger.warn("Illegal state: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            ex.getMessage(), 
            HttpStatus.BAD_REQUEST.value(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        logger.warn("Illegal argument: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            ex.getMessage(), 
            HttpStatus.BAD_REQUEST.value(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        logger.error("Runtime exception: ", ex);
        
        Map<String, Object> response = createErrorResponse(
            ex.getMessage() != null ? ex.getMessage() : "Runtime error occurred", 
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {
        
        logger.error("Unexpected error: ", ex);
        
        Map<String, Object> response = createErrorResponse(
            "An unexpected error occurred", 
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Create a standardized error response
     */
    private Map<String, Object> createErrorResponse(String message, int status, String path) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", status);
        response.put("error", HttpStatus.valueOf(status).getReasonPhrase());
        response.put("message", message);
        response.put("path", path.replace("uri=", ""));
        return response;
    }
}
