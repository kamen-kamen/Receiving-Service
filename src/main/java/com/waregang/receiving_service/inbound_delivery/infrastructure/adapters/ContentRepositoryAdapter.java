package com.waregang.receiving_service.inbound_delivery.infrastructure.adapters;

import com.waregang.receiving_service.inbound_delivery.domain.ports.ContentRepositoryPort;
import com.waregang.receiving_service.inbound_delivery.infrastructure.ContentRepositoryJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ContentRepositoryAdapter implements ContentRepositoryPort {

    private final ContentRepositoryJpa jpaRepository;

    @Override
    public boolean existsBySkuAndInboundDeliveryId(String sku, UUID deliveryId) {
        return jpaRepository.existsBySkuAndInboundDeliveryId(sku, deliveryId);
    }
}