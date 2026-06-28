package com.waregang.receiving_service.inbound_delivery.infrastructure;

import com.waregang.receiving_service.inbound_delivery.domain.model.HandlingUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HandlingUnitRepository extends JpaRepository<HandlingUnit, UUID> {
    boolean existsByLpnAndInboundDeliveryId(String lpn, UUID inboundDeliveryId);
}
