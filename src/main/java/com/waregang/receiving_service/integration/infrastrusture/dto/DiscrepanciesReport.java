package com.waregang.receiving_service.integration.infrastrusture.dto;

import java.util.List;
import java.util.UUID;

public record DiscrepanciesReport(
        UUID inboundDeliveryId,
        UUID goodsReceiptId,
        List<DiscrepancyLine> discrepancyLines
) {}
