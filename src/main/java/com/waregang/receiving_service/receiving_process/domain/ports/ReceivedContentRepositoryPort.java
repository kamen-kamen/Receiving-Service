package com.waregang.receiving_service.receiving_process.domain.ports;

import com.waregang.receiving_service.integration.infrastrusture.dto.SkuQuantityDto;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivedContent;

import java.util.List;
import java.util.UUID;

public interface ReceivedContentRepositoryPort {
    ReceivedContent save(ReceivedContent content);
    List<ReceivedContent> findAll();
    List<SkuQuantityDto> findActualSkuQuantitiesByReceiptId(UUID receiptId);
}