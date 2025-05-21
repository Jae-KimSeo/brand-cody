package org.service.brandcody.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException e) {
        ErrorResponse response = ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorResponse response = ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        ErrorResponse response = ErrorResponse.of(HttpStatus.BAD_REQUEST, errorMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .findFirst()
                .orElse("Validation error");
        
        ErrorResponse response = ErrorResponse.of(HttpStatus.BAD_REQUEST, errorMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String errorMessage = "Data integrity violation";

        if (e.getCause() instanceof ConstraintViolationException constraintException) {
            String constraintName = constraintException.getConstraintViolations().toString();
            
            if (constraintName != null) {
                if (constraintName.contains("idx_brand_name")) {
                    errorMessage = "Brand name must be unique";
                }
            }
        }
        
        log.warn("Data integrity violation: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(HttpStatus.CONFLICT, errorMessage);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException e) {
        String entityName = Objects.requireNonNull(e.getPersistentClassName()).substring(e.getPersistentClassName().lastIndexOf('.') + 1);
        String errorMessage = entityName + " with id " + e.getIdentifier() + 
                " was updated by another transaction. Please try again.";
        
        log.warn("Optimistic locking failure: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(HttpStatus.CONFLICT, errorMessage);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception e) {
        String errorMessage = "An unexpected error occurred: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        ErrorResponse response = ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);

        log.error("Unhandled exception occurred", e);
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}