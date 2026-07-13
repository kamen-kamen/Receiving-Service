package com.waregang.receiving_service.inbound_delivery.infrastructure.adapters;

import com.waregang.receiving_service.inbound_delivery.domain.ports.HandlingUnitRepositoryPort;
import com.waregang.receiving_service.inbound_delivery.infrastructure.jpa_repositories.HandlingUnitRepositoryJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class HandlingUnitRepositoryAdapter implements HandlingUnitRepositoryPort {

    private final HandlingUnitRepositoryJpa jpaRepository;

    @Override
    public boolean existsByLpnAndInboundDeliveryId(String lpn, UUID deliveryId) {
        return jpaRepository.existsByLpnAndInboundDeliveryId(lpn, deliveryId);
    }
}