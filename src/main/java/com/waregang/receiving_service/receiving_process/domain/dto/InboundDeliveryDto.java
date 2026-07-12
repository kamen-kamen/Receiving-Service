package com.waregang.receiving_service.receiving_process.domain.dto;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivingMode;

import java.util.UUID;

public record InboundDeliveryDto(
        UUID id,
        String asnNumber,
        String warehouseId,
        ReceivingMode receivingMode
) {
}