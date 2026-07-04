package com.waregang.receiving_service.receiving_process.api.dto;

public record ScanContentRequest(
        String sku,
        Long quantity
) {}
