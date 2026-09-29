package com.kido.corporation.auth.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kido.corporation.auth.constant.HTTPCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    @Builder.Default
    HTTPCode code = HTTPCode.SUCCESS;
    
    String message;
    T result;

    public static <T> ApiResponse<T> success(T result) {
        return ApiResponse.<T>builder()
                .code(HTTPCode.SUCCESS)
                .message(HTTPCode.SUCCESS.getMessage())
                .result(result)
                .build();
    }

    public static <T> ApiResponse<T> success(HTTPCode code, T result) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(code.getMessage())
                .result(result)
                .build();
    }

    public static <T> ApiResponse<T> error(HTTPCode code) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(code.getMessage())
                .build();
    }

    public static <T> ApiResponse<T> error(HTTPCode code, String message, T details) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message != null ? message : code.getMessage())
                .result(details)
                .build();
    }
}
