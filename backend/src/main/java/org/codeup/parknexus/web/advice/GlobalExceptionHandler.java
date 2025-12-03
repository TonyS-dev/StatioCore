package org.codeup.parknexus.web.advice;

import jakarta.servlet.http.HttpServletRequest;
import org.codeup.parknexus.exception.BadRequestException;
import org.codeup.parknexus.exception.ConflictException;
import org.codeup.parknexus.exception.ResourceNotFoundException;
import org.codeup.parknexus.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Error 404: when a requested resource is not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setType(URI.create("/errors/not-found"));
        pd.setTitle("Resource Not Found");
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("instance", req.getRequestURI());
        log.warn("Resource not found: {}", ex.getMessage());
        return pd;
    }

    // Error 409: Conflict, e.g., when trying to create a resource that already exists
    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setType(URI.create("/errors/conflict"));
        pd.setTitle("Data Conflict");
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("instance", req.getRequestURI());
        log.warn("Data conflict: {}", ex.getMessage());
        return pd;
    }

    // Error 400: Bad request
    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setType(URI.create("/errors/bad-request"));
        pd.setTitle("Bad Request");
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("instance", req.getRequestURI());
        log.warn("Bad request: {}", ex.getMessage());
        return pd;
    }

    // Error 401: Unauthorized
    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setType(URI.create("/errors/unauthorized"));
        pd.setTitle("Unauthorized");
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("instance", req.getRequestURI());
        log.warn("Unauthorized: {}", ex.getMessage());
        return pd;
    }

    // Error 403: Forbidden (manual throws, not security filter chain)
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setType(URI.create("/errors/forbidden"));
        pd.setTitle("Forbidden");
        pd.setDetail("Access denied. You don't have permission to access this resource.");
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("instance", req.getRequestURI());
        log.warn("Access denied: {}", ex.getMessage());
        return pd;
    }

    // Error 400: Illegal arguments passed to methods
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setType(URI.create("/errors/bad-request"));
        pd.setTitle("Bad Request");
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("instance", req.getRequestURI());
        log.warn("Illegal argument: {}", ex.getMessage());
        return pd;
    }

    // Error 400: DTO validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setType(URI.create("/errors/validation"));
        pd.setTitle("Validation Failed");
        pd.setDetail("One or more fields are invalid.");
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("instance", req.getRequestURI());

        List<Map<String, Object>> errors = ex.getBindingResult()
                .getFieldErrors().stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", Optional.ofNullable(error.getDefaultMessage()).orElse("invalid"),
                        "rejectedValue", Optional.ofNullable(error.getRejectedValue()).orElse("")
                ))
                .toList();
        pd.setProperty("errors", errors);
        log.warn("Validation errors: {}", errors);
        return pd;
    }

    // Error 400: When the JSON in the request body is malformed
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setType(URI.create("/errors/bad-request"));
        pd.setTitle("Malformed JSON Request");
        pd.setDetail("The request body could not be parsed.");
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("instance", req.getRequestURI());
        log.warn("Malformed JSON request: {}", ex.getMessage());
        return pd;
    }

    // Error 401: Authentication failures (manual throws, filter chain uses JwtAuthenticationEntryPoint)
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setType(URI.create("/errors/unauthorized"));
        pd.setTitle("Authentication Failed");
        pd.setDetail("Invalid credentials provided.");
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("instance", req.getRequestURI());
        log.warn("Authentication failure: {}", ex.getMessage());
        return pd;
    }

    // Error 500: Generic server error handler
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setType(URI.create("/errors/internal"));
        pd.setTitle("Internal Server Error");
        pd.setDetail("An unexpected error occurred.");
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("instance", req.getRequestURI());
        log.error("Unhandled exception at {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return pd;
    }
}
