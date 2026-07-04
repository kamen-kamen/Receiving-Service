package com.waregang.receiving_service.receiving_process.infrastructure.jpa_repositories;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivedContentJpa;
import com.waregang.receiving_service.integration.infrastrusture.dto.SkuQuantityDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReceivedContentRepositoryJpa extends JpaRepository<ReceivedContentJpa, UUID> {

    @Query(nativeQuery = true,
            value = """
            SELECT rc.sku, SUM(rc.quantity)::bigint 
            FROM received_contents rc
            JOIN received_handling_units rhu ON rc.container_unit_id = rhu.id
            JOIN worker_receiving_sessions wrc ON wrc.id = rhu.worker_receiving_session_id
            WHERE wrc.receipt_id = :receiptId
            GROUP BY rc.sku
            """)
    List<SkuQuantityDto> findActualSkuQuantitiesByReceiptId(@Param("receiptId") UUID receiptId);
}
//SELECT new com.waregang.receiving_service.integration.infrastrusture.dto.SkuQuantityDto(rc.sku, SUM(rc.quantity))
//FROM ReceivedContentJpa rc
//WHERE rc.containerUnit.workerSession.receiptId = :receiptId
//GROUP BY rc.sku