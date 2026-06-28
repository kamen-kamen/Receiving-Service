package com.waregang.receiving_service.inbound_delivery.infrastructure;

import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDeliveryStatus;
import com.waregang.receiving_service.integration.infrastrusture.dto.SkuQuantityDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InboundDeliveryRepository extends JpaRepository<InboundDelivery, UUID> {
    Optional<InboundDelivery> findByAsnNumber(String asn);

    boolean existsByIdAndStatus(UUID inboundDeliveryId, InboundDeliveryStatus inboundDeliveryStatus);

    @Query("""
            SELECT SkuQuantityDto(c.sku, SUM(c.quantity))
            FROM Content c
            WHERE c.containerUnit.inboundDelivery.id = :deliveryId
            GROUP BY c.sku
            """)
    List<SkuQuantityDto> findExpectedSkuQuantities(@Param("deliveryId") UUID deliveryId);

    @Query("""
        SELECT d.status
        FROM InboundDelivery d
        WHERE d.id = :deliveryId
""")
    Optional<InboundDeliveryStatus> findDeliveryStatusById(@Param("deliveryId") UUID deliveryId);
}
