package com.waregang.receiving_service.receiving_process.api.dto;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivingMode;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record StartReceivingResponse(
        @NotBlank
        UUID receiptId,

        @NotBlank
        ReceivingMode mode
) {}
