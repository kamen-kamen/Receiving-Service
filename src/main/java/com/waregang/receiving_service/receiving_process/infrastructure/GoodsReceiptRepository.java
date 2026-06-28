package com.waregang.receiving_service.receiving_process.infrastructure;

import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceipt;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceiptStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, UUID> {
    List<GoodsReceipt> findAllByStatusAndWarehouseId(GoodsReceiptStatus status, String warehouseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<GoodsReceipt> findWithLockById(UUID receiptId);
}
