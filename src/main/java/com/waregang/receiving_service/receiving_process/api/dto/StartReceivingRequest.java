package com.waregang.receiving_service.receiving_process.api.dto;

import jakarta.validation.constraints.NotBlank;

public record StartReceivingRequest(
        @NotBlank
        String asnNumber,

        @NotBlank
        String gateNumber
) {}
