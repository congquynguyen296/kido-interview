package com.kido.corporation.auth.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum HTTPCode {
    SUCCESS(1000, "Success", HttpStatus.OK),
    CREATED(1001, "Created successfully", HttpStatus.CREATED),
    NO_CONTENT(1004, "No content", HttpStatus.NO_CONTENT),

    INVALID_REQUEST(2000, "Invalid request", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED(2001, "Validation failed", HttpStatus.BAD_REQUEST),
    MALFORMED_JSON(2002, "Malformed JSON", HttpStatus.BAD_REQUEST),

    UNAUTHENTICATED(3000, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(3001, "Invalid credentials", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(3002, "Token has expired", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(3003, "Invalid token", HttpStatus.UNAUTHORIZED),
    TOKEN_REVOKED(3004, "Token has been revoked", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_MISSING(3005, "Refresh token is missing", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED(3006, "Account is locked", HttpStatus.LOCKED),
    ACCOUNT_DISABLED(3007, "Account is disabled", HttpStatus.FORBIDDEN),
    EMAIL_NOT_VERIFIED(3008, "Email is not verified", HttpStatus.FORBIDDEN),

    FORBIDDEN(4000, "Access denied", HttpStatus.FORBIDDEN),

    USER_NOT_FOUND(5000, "User not found", HttpStatus.NOT_FOUND),
    ROLE_NOT_FOUND(5001, "Role not found", HttpStatus.NOT_FOUND),
    PERMISSION_NOT_FOUND(5002, "Permission not found", HttpStatus.NOT_FOUND),

    EMAIL_ALREADY_EXISTS(6000, "Email already exists", HttpStatus.CONFLICT),
    USERNAME_ALREADY_EXISTS(6001, "Username already exists", HttpStatus.CONFLICT),
    ROLE_ALREADY_EXISTS(6002, "Role already exists", HttpStatus.CONFLICT),
    PERMISSION_ALREADY_EXISTS(6003, "Permission already exists", HttpStatus.CONFLICT),
    ROLE_IN_USE(6004, "Role is in use", HttpStatus.CONFLICT),

    PASSWORD_MISMATCH(7000, "Password mismatch", HttpStatus.BAD_REQUEST),
    OLD_PASSWORD_INCORRECT(7001, "Old password incorrect", HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_SAME_AS_OLD(7002, "New password cannot be same as old password", HttpStatus.BAD_REQUEST),
    WEAK_PASSWORD(7003, "Password is too weak", HttpStatus.BAD_REQUEST),

    OTP_INVALID(8000, "Invalid OTP", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(8001, "OTP has expired", HttpStatus.BAD_REQUEST),
    OTP_TOO_MANY_ATTEMPTS(8002, "Too many OTP attempts", HttpStatus.TOO_MANY_REQUESTS),
    OTP_RESEND_TOO_SOON(8003, "OTP resend too soon", HttpStatus.TOO_MANY_REQUESTS),
    RESET_TOKEN_INVALID(8004, "Invalid reset token", HttpStatus.BAD_REQUEST),

    INTERNAL_ERROR(9000, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    TOO_MANY_REQUESTS(9001, "Too many requests", HttpStatus.TOO_MANY_REQUESTS),
    OPERATION_NOT_ALLOWED(9002, "Operation not allowed", HttpStatus.FORBIDDEN);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    HTTPCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @JsonValue
    public int getCode() {
        return code;
    }
}
