package com.waregang.receiving_service.receiving_process.domain.dto;

import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceiptStatus;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivingMode;

import java.util.UUID;

public record GoodsReceiptDto(
        UUID id,
        GoodsReceiptStatus status,
        String warehouseId,
        String gateNumber,
        UUID managerId,
        ReceivingMode receivingMode,
        UUID inboundDeliveryId
) {}