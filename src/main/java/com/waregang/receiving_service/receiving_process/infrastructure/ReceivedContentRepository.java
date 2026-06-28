package com.waregang.receiving_service.receiving_process.infrastructure;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivedContent;
import com.waregang.receiving_service.integration.infrastrusture.dto.SkuQuantityDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReceivedContentRepository extends JpaRepository<ReceivedContent, UUID> {

    @Query("""
            SELECT new com.waregang.receiving_service.integration.infrastrusture.dto.SkuQuantityDto(rc.sku, SUM(rc.quantity))
            FROM ReceivedContent rc
            WHERE rc.containerUnit.workerSession.receiptId = :receiptId
            GROUP BY rc.sku
            """)
    List<SkuQuantityDto> findActualSkuQuantitiesByReceiptId(@Param("receiptId") UUID receiptId);
}
