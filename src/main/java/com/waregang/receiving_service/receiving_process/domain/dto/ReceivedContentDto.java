package com.waregang.receiving_service.receiving_process.domain.dto;

public record ReceivedContentDto(
        String containerLpn,
        String sku,
        Long quantity
) {}
