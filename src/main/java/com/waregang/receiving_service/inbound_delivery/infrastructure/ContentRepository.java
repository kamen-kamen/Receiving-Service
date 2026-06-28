package com.waregang.receiving_service.inbound_delivery.infrastructure;

import com.waregang.receiving_service.inbound_delivery.domain.model.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ContentRepository extends JpaRepository<Content, UUID> {
    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM Content c
            JOIN c.containerUnit hu
            WHERE c.sku = :sku AND hu.inboundDelivery.id = :inboundDeliveryId
            """)
    boolean existsBySkuAndInboundDeliveryId(@Param("sku") String sku, @Param("inboundDeliveryId") UUID inboundDeliveryId);
}
