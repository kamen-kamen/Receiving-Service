package com.waregang.receiving_service.common.exception_handling.error_code;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    HttpStatus getHttpStatus();
    String getCode();
}
