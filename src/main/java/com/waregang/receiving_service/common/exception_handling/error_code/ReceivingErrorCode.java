package com.waregang.receiving_service.common.exception_handling.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReceivingErrorCode implements ErrorCode {

    // Goods Receipt
    RECEIPT_NOT_FOUND(HttpStatus.NOT_FOUND, "goods-receipt.not-found"),
    RECEIPT_INVALID_STATE(HttpStatus.CONFLICT, "goods-receipt.invalid-state"),

    // Worker Session
    WORKER_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "worker-session.not-found"),
    WORKER_SESSION_INVALID_STATE(HttpStatus.CONFLICT, "worker-session.invalid-state"),
    WORKER_ALREADY_JOINED(HttpStatus.CONFLICT, "worker-session.already-joined"),
    WORKER_SESSION_ALREADY_COMPLETED(HttpStatus.CONFLICT, "worker-session.already-completed"),
    // Scanning
    SCANNED_HU_NOT_IN_THE_DELIVERY(HttpStatus.CONFLICT, "scanning.unit-not-in-the-delivery"),
    CONTENT_NOT_IN_THE_DELIVERY(HttpStatus.CONFLICT, "scanning.content-not-in-the-delivery"),
    WRONG_QUANTITY(HttpStatus.CONFLICT, "scanning.wrong-quantity"),
    LPN_ALREADY_SCANNED(HttpStatus.CONFLICT, "scanning.duplicate-lpn"),
    DUPLICATE_SKU_SCAN(HttpStatus.CONFLICT, "scanning.duplicate-sku"),

    // General Receiving
    WAREHOUSE_MISMATCH(HttpStatus.CONFLICT, "receiving.warehouse-mismatch"),
    EMPTY_LPN_NOT_ALLOWED(HttpStatus.CONFLICT, "receiving.mode-constraint"),
    PREVIOUS_UNIT_NOT_FOUND(HttpStatus.NOT_FOUND, "receiving.previous-unit-not-found"),

    // Put Away
    PUT_AWAY_UNITS_NOT_FOUND(HttpStatus.NOT_FOUND, "put-away.units-not-found"),

    // Inbound Delivery
    DELIVERY_CONCURRENT_MODIFICATION(HttpStatus.CONFLICT, "inbound-delivery.change-conflict");





    private final HttpStatus httpStatus;
    private final String code;
}
