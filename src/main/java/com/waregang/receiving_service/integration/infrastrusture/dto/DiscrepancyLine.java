package com.waregang.receiving_service.integration.infrastrusture.dto;

public record DiscrepancyLine(
        String sku,
        long expected,
        long actual,
        DiscrepancyType type
) {
}
