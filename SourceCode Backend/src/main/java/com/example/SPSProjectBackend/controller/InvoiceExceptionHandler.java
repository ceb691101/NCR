package com.example.SPSProjectBackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = InvoiceController.class)
public class InvoiceExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException exception) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("error", exception.getStatusCode().toString());
        response.put("message", exception.getReason());
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(exception.getStatusCode()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage()));

        response.put("success", false);
        response.put("error", "BAD_REQUEST");
        response.put("message", "Invoice request validation failed");
        response.put("details", fieldErrors);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception exception) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("error", "INTERNAL_SERVER_ERROR");
        response.put("message", exception.getMessage() == null ? "Failed to generate invoice" : exception.getMessage());
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}