package com.waregang.receiving_service.inbound_delivery.api.dto;

import java.util.UUID;

public record CreateDeliveryResponse(
        UUID inboundDeliveryId
) {
}
