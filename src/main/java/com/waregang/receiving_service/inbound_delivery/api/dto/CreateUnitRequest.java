package com.waregang.receiving_service.inbound_delivery.api.dto;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

public record CreateUnitRequest(
        @NotBlank
        String type,

        @NotBlank
        String lpn,

        @Nullable
        String parentLpn
) {}
