package com.waregang.receiving_service.inbound_delivery.infrastructure.jpa_repositories;

import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDeliveryStatus;
import com.waregang.receiving_service.inbound_delivery.infrastructure.jpa_entities.InboundDeliveryJpa;
import com.waregang.receiving_service.integration.infrastrusture.dto.SkuQuantityDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InboundDeliveryRepositoryJpa extends JpaRepository<InboundDeliveryJpa, UUID> {
    Optional<InboundDeliveryJpa> findByAsnNumber(String asn);

    boolean existsByIdAndStatus(UUID inboundDeliveryId, InboundDeliveryStatus inboundDeliveryStatus);

    @Query("""
            SELECT new com.waregang.receiving_service.integration.infrastrusture.dto.SkuQuantityDto(c.sku, SUM(c.quantity))
            FROM ContentJpa c
            WHERE c.containerUnit.inboundDelivery.id = :deliveryId
            GROUP BY c.sku
            """)
    List<SkuQuantityDto> findExpectedSkuQuantities(@Param("deliveryId") UUID deliveryId);

    @Query("""
        SELECT d.status
        FROM InboundDeliveryJpa d
        WHERE d.id = :deliveryId
""")
    Optional<InboundDeliveryStatus> findDeliveryStatusById(@Param("deliveryId") UUID deliveryId);
}