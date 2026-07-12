package com.waregang.receiving_service.receiving_process.infrastructure.jpa_repositories;

import com.waregang.receiving_service.integration.infrastrusture.dto.SkuQuantityDto;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.ReceivedContentJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReceivedContentRepositoryJpa extends JpaRepository<ReceivedContentJpa, UUID> {

    @Query(nativeQuery = true,
            value = """
            SELECT
                rc.sku,
                SUM(rc.quantity) as quantity
            FROM 
                received_contents rc
            JOIN 
                received_handling_units rhu 
            ON 
                rc.container_unit_id = rhu.id
            WHERE 
                rhu.receipt_id = :receiptId
            GROUP BY 
                rc.sku
            """)
    List<SkuQuantityDto> findActualSkuQuantitiesByReceiptId(@Param("receiptId") UUID receiptId);
}