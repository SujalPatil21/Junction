package com.junction.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the JUNCTION backend.
 *
 * <p>All API errors are returned as structured JSON with consistent shape:
 * <pre>
 * {
 *   "error":     "ERROR_CODE",
 *   "message":   "Human-readable description",
 *   "path":      "/api/endpoint",
 *   "timestamp": "2026-11-06T17:00:00Z",
 *   "details":   [ ... ]  // optional, only for validation errors
 * }
 * </pre>
 *
 * <p>Stack traces are NEVER exposed in responses (configured in application.yml:
 * {@code server.error.include-stacktrace: never}).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── Application-specific exceptions ───────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
        ResourceNotFoundException ex, HttpServletRequest request
    ) {
        return errorResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request, null);
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRuleViolation(
        BusinessRuleViolationException ex, HttpServletRequest request
    ) {
        log.warn("Business rule violation at {}: {}", request.getServletPath(), ex.getMessage());
        return errorResponse(HttpStatus.CONFLICT, "BUSINESS_RULE_VIOLATION", ex.getMessage(), request, null);
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidStateTransition(
        InvalidStateTransitionException ex, HttpServletRequest request
    ) {
        return errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_STATE_TRANSITION", ex.getMessage(), request, null);
    }

    // ── Validation errors ──────────────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
        MethodArgumentNotValidException ex, HttpServletRequest request
    ) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.toList());

        return errorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Request validation failed", request, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
        ConstraintViolationException ex, HttpServletRequest request
    ) {
        List<String> details = ex.getConstraintViolations().stream()
            .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
            .collect(Collectors.toList());

        return errorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Constraint violation", request, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMessageNotReadable(
        HttpMessageNotReadableException ex, HttpServletRequest request
    ) {
        return errorResponse(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Request body is missing or malformed", request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
        MethodArgumentTypeMismatchException ex, HttpServletRequest request
    ) {
        String message = "Invalid value '%s' for parameter '%s'".formatted(ex.getValue(), ex.getName());
        return errorResponse(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", message, request, null);
    }

    // ── Security errors ────────────────────────────────────────────────────────

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
        AuthenticationException ex, HttpServletRequest request
    ) {
        // Do not expose internal authentication error details
        return errorResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication required", request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
        AccessDeniedException ex, HttpServletRequest request
    ) {
        return errorResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", "Insufficient permissions", request, null);
    }

    // ── Catch-all ─────────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(
        Exception ex, HttpServletRequest request
    ) {
        log.error("Unhandled exception at {}: {}", request.getServletPath(), ex.getMessage(), ex);
        return errorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_ERROR",
            "An unexpected error occurred",
            request,
            null
        );
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private ResponseEntity<Map<String, Object>> errorResponse(
        HttpStatus status,
        String errorCode,
        String message,
        HttpServletRequest request,
        List<String> details
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", errorCode);
        body.put("message", message);
        body.put("path", request.getServletPath());
        body.put("timestamp", Instant.now().toString());
        if (details != null && !details.isEmpty()) {
            body.put("details", details);
        }
        return ResponseEntity.status(status).body(body);
    }
}

