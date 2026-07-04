package com.waregang.receiving_service.receiving_process.infrastructure.adapters;

import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceipt;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceiptStatus;
import com.waregang.receiving_service.receiving_process.domain.ports.GoodsReceiptRepositoryPort;
import com.waregang.receiving_service.receiving_process.infrastructure.dto.GoodsReceiptDto;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.GoodsReceiptJpa;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_repositories.GoodsReceiptRepositoryJpa;
import com.waregang.receiving_service.receiving_process.infrastructure.mappers.GoodsReceiptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor

@Repository
public class GoodsReceiptRepositoryAdapter implements GoodsReceiptRepositoryPort {
    private final GoodsReceiptRepositoryJpa repositoryJpa;

    private final GoodsReceiptMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public GoodsReceipt save(GoodsReceipt receipt) {
        GoodsReceiptJpa saved = repositoryJpa.save(mapper.toJpa(receipt));
        
        receipt.pullDomainEvents().forEach(eventPublisher::publishEvent);
        
        return mapper.toDomain(saved);
    }

    @Override
    public GoodsReceipt update(GoodsReceipt receipt) {
        GoodsReceiptJpa receiptJpa = repositoryJpa.findById(receipt.getId())
                .orElseThrow(() -> AppException.of(ReceivingErrorCode.RECEIPT_NOT_FOUND)
                        .with("receipt_id", receipt.getId()));
        
        mapper.updateJpaFromDomain(receiptJpa, receipt);

        receipt.pullDomainEvents().forEach(eventPublisher::publishEvent);
        
        return mapper.toDomain(receiptJpa);
    }

    @Override
    public Optional<GoodsReceipt> findWithLockById(UUID receiptId) {
        return repositoryJpa.findWithLockById(receiptId).map(mapper::toDomain);
    }

    @Override
    public List<GoodsReceiptDto> findAllByStatusAndWarehouseId(GoodsReceiptStatus receiptStatus, String warehouseId) {
        var receipts = repositoryJpa.findAllByStatusAndWarehouseId(receiptStatus, warehouseId);

        return receipts.stream()
                .map(mapper::toGoodsReceiptDto)
                .toList();
    }

    @Override
    public Optional<GoodsReceipt> findById(UUID receiptId) {
        return repositoryJpa.findById(receiptId).map(mapper::toDomain);
    }
}