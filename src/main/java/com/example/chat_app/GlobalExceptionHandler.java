package com.example.chat_app;

import com.example.chat_app.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Custom Exception Handlers
    @ExceptionHandler({
            ValidationException.class,
            EmptyCredentialsException.class,
            MissingServletRequestParameterException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestExceptions(Exception ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return new ErrorResponse("bad_request", ex.getMessage());
    }

    @ExceptionHandler({
            UsernameAlreadyExistsException.class,
            EmailAlreadyExistsException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictExceptions(RuntimeException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return new ErrorResponse("conflict", ex.getMessage());
    }

    // Security Exception Handlers
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(UsernameNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        return new ErrorResponse("user_not_found", ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleBadCredentials(BadCredentialsException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return new ErrorResponse("authentication_failed", "Invalid username or password");
    }

    @ExceptionHandler({
            DisabledException.class,
            LockedException.class
    })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAccountStatusExceptions(Exception ex) {
        log.warn("Account status issue: {}", ex.getMessage());
        return new ErrorResponse("account_issue", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return new ErrorResponse("forbidden", "You don't have permission to access this resource");
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication exception: {}", ex.getMessage());
        return new ErrorResponse("authentication_error", ex.getMessage());
    }

    // Validation Exception Handlers
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() == null ?
                                "Invalid value" : fieldError.getDefaultMessage()
                ));

        log.warn("Validation errors: {}", errors);
        return new ValidationErrorResponse("validation_failed", "Invalid request parameters", errors);
    }

//    @ExceptionHandler(ConstraintViolationException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ValidationErrorResponse handleConstraintViolation(ConstraintViolationException ex) {
//        Map<String, String> errors = ex.getConstraintViolations()
//                .stream()
//                .collect(Collectors.toMap(
//                        violation -> violation.getPropertyPath().toString(),
//                        violation -> violation.getMessage()
//                ));
//
//        log.warn("Constraint violations: {}", errors);
//        return new ValidationErrorResponse("constraint_violation", "Invalid data", errors);
//    }

    // System Exception Handlers
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleRequestParsingExceptions(Exception ex) {
        log.warn("Request parsing error: {}", ex.getMessage());
        return new ErrorResponse("invalid_request", "Malformed request data");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnknownErrors(Exception ex) {
        log.error("Unexpected error: ", ex);
        return new ErrorResponse("internal_error", "An unexpected error occurred");
    }

    // Response DTOs
    public record ErrorResponse(String code, String message) {}

    public record ValidationErrorResponse(
            String code,
            String message,
            Map<String, String> errors
    ) {}
}