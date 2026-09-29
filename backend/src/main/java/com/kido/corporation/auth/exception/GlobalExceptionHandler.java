package com.kido.corporation.auth.exception;

import com.kido.corporation.auth.constant.HTTPCode;
import com.kido.corporation.auth.dto.common.ApiResponse;
import com.kido.corporation.auth.dto.common.FieldErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        log.error("AppException: {}", ex.getMessage());
        return ResponseEntity.status(ex.getCode().getHttpStatus())
                .body(ApiResponse.error(ex.getCode(), ex.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<FieldErrorResponse>>> handleValidationException(MethodArgumentNotValidException ex) {
        List<FieldErrorResponse> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(HTTPCode.VALIDATION_FAILED.getHttpStatus())
                .body(ApiResponse.error(HTTPCode.VALIDATION_FAILED, HTTPCode.VALIDATION_FAILED.getMessage(), errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("ConstraintViolationException: {}", ex.getMessage());
        return ResponseEntity.status(HTTPCode.VALIDATION_FAILED.getHttpStatus())
                .body(ApiResponse.error(HTTPCode.VALIDATION_FAILED, ex.getMessage(), null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("HttpMessageNotReadableException: {}", ex.getMessage());
        return ResponseEntity.status(HTTPCode.MALFORMED_JSON.getHttpStatus())
                .body(ApiResponse.error(HTTPCode.MALFORMED_JSON));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.error("AuthenticationException: {}", ex.getMessage());
        return ResponseEntity.status(HTTPCode.UNAUTHENTICATED.getHttpStatus())
                .body(ApiResponse.error(HTTPCode.UNAUTHENTICATED));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("AccessDeniedException: {}", ex.getMessage());
        return ResponseEntity.status(HTTPCode.FORBIDDEN.getHttpStatus())
                .body(ApiResponse.error(HTTPCode.FORBIDDEN));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("DataIntegrityViolationException: {}", ex.getMessage());
        return ResponseEntity.status(HTTPCode.EMAIL_ALREADY_EXISTS.getHttpStatus())
                .body(ApiResponse.error(HTTPCode.EMAIL_ALREADY_EXISTS, "Data integrity violation (conflict)", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("Unhandled Exception: ", ex);
        return ResponseEntity.status(HTTPCode.INTERNAL_ERROR.getHttpStatus())
                .body(ApiResponse.error(HTTPCode.INTERNAL_ERROR));
    }
}
