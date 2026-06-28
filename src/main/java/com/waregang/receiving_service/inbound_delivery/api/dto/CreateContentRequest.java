package com.waregang.receiving_service.inbound_delivery.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateContentRequest(
        @NotBlank
        String parentLpn,

        @NotBlank
        String sku,

        @Min(1)
        int quantity
) {
}
