package com.waregang.receiving_service.receiving_process.domain.ports;

import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceipt;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceiptStatus;
import com.waregang.receiving_service.receiving_process.domain.dto.GoodsReceiptDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoodsReceiptRepositoryPort {
    GoodsReceipt save(GoodsReceipt receipt);

    GoodsReceipt update(GoodsReceipt receipt);

    Optional<GoodsReceipt> findWithLockById(UUID receiptId);

    List<GoodsReceiptDto> findAllDtosByStatusAndWarehouseId(GoodsReceiptStatus receiptStatus, String warehouseId);

    Optional<GoodsReceipt> findById(UUID receiptId);
}