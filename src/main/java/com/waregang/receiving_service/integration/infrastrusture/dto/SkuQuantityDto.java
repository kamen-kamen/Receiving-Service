package com.waregang.receiving_service.integration.infrastrusture.dto;

public record SkuQuantityDto(
        String sku,
        Long quantity
) {
}