package com.kido.corporation.auth.exception;

import com.kido.corporation.auth.constant.HTTPCode;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final HTTPCode code;

    public AppException(HTTPCode code) {
        super(code.getMessage());
        this.code = code;
    }

    public AppException(HTTPCode code, String message) {
        super(message);
        this.code = code;
    }
}
