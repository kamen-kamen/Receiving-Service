package com.waregang.receiving_service.receiving_process.domain.event;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivingMode;

import java.util.UUID;

public record OpenedGoodsReceiptEvent(
        UUID receiptId,
        String asnNumber,
        String gateNumber,
        String warehouseId,
        String managerNickname,
        ReceivingMode mode
) {}
