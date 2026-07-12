package com.waregang.receiving_service.receiving_process.infrastructure.jpa_repositories;

import com.waregang.receiving_service.receiving_process.domain.dto.GoodsReceiptDto;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceiptStatus;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.GoodsReceiptJpa;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoodsReceiptRepositoryJpa extends JpaRepository<GoodsReceiptJpa, UUID> {

    @Query("""
            SELECT new com.waregang.receiving_service.receiving_process.domain.dto.GoodsReceiptDto(
                gr.id,
                gr.status,
                gr.warehouseId,
                gr.gateNumber,
                gr.managerId,
                id.receivingMode,
                gr.inboundDeliveryId
            )
            FROM GoodsReceiptJpa gr
            JOIN InboundDelivery id ON gr.inboundDeliveryId = id.id
            WHERE gr.status = :status AND gr.warehouseId = :warehouseId
            """)
    List<GoodsReceiptDto> findAllDtosByStatusAndWarehouseId(
            @Param("status") GoodsReceiptStatus status,
            @Param("warehouseId") String warehouseId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<GoodsReceiptJpa> findWithLockById(UUID receiptId);
}