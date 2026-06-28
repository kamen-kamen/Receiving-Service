package com.waregang.receiving_service.common.exception_handling.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InboundDeliveryErrorCode implements ErrorCode {

    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "inbound-delivery.not-found"),
    PARENT_LPN_MISSING(HttpStatus.BAD_REQUEST, "inbound-delivery.parent-lpn-missing"),
    INVALID_STATE(HttpStatus.CONFLICT, "inbound-delivery.illegal-state" );

    private final HttpStatus httpStatus;
    private final String code;
}
