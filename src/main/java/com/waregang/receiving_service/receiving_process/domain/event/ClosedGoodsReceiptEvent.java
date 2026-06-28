package com.waregang.receiving_service.receiving_process.domain.event;

import java.util.UUID;

public record ClosedGoodsReceiptEvent(
        UUID receiptId,
        UUID inboundDeliveryId,
        String gateNumber
) {}
