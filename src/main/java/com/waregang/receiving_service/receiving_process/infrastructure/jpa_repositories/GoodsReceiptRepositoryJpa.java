package com.waregang.receiving_service.receiving_process.infrastructure.jpa_repositories;

import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceiptStatus;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.GoodsReceiptJpa;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoodsReceiptRepositoryJpa extends JpaRepository<GoodsReceiptJpa, UUID> {
    List<GoodsReceiptJpa> findAllByStatusAndWarehouseId(GoodsReceiptStatus status, String warehouseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<GoodsReceiptJpa> findWithLockById(UUID receiptId);
}
