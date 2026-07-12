package com.waregang.receiving_service.inbound_delivery.domain.ports;

import java.util.UUID;

public interface ContentRepositoryPort {
    boolean existsBySkuAndInboundDeliveryId(String sku, UUID deliveryId);
}