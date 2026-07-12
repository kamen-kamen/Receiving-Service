package com.waregang.receiving_service.inbound_delivery.infrastructure;

import com.waregang.receiving_service.inbound_delivery.infrastructure.jpa_entities.HandlingUnitJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HandlingUnitRepositoryJpa extends JpaRepository<HandlingUnitJpa, UUID> {
    boolean existsByLpnAndInboundDeliveryId(String lpn, UUID inboundDeliveryId);
}