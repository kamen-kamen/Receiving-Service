package com.waregang.receiving_service.common.exception_handling;

import com.waregang.receiving_service.common.exception_handling.error_code.ErrorCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, Object> errorContext = new HashMap<>();

    private AppException(ErrorCode errorCode) {
        super(errorCode.getCode());
        this.errorCode = errorCode;
    }


    public static AppException of(ErrorCode code) {
        return new AppException(code);
    }

    public AppException with(String key, Object value) {
        this.errorContext.put(key, value);
        return this;
    }
}
