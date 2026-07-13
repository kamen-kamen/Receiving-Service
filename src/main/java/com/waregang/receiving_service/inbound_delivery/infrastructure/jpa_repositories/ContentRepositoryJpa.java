package com.waregang.receiving_service.inbound_delivery.infrastructure.jpa_repositories;

import com.waregang.receiving_service.inbound_delivery.infrastructure.jpa_entities.ContentJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ContentRepositoryJpa extends JpaRepository<ContentJpa, UUID> {
    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM ContentJpa c
            JOIN c.containerUnit hu
            WHERE c.sku = :sku AND hu.inboundDelivery.id = :inboundDeliveryId
            """)
    boolean existsBySkuAndInboundDeliveryId(@Param("sku") String sku,
                                            @Param("inboundDeliveryId") UUID inboundDeliveryId);
}