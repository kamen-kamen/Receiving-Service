package com.waregang.receiving_service.receiving_process.infrastructure.dto;

public record ReceivedContentDto(
        String containerLpn,
        String sku,
        Long quantity
) {}
