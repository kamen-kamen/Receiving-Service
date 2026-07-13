package com.waregang.receiving_service.receiving_process.infrastructure.adapters;

import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivedContent;
import com.waregang.receiving_service.receiving_process.domain.ports.ReceivedContentRepositoryPort;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.ReceivedContentJpa;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_repositories.ReceivedContentRepositoryJpa;
import com.waregang.receiving_service.receiving_process.infrastructure.mappers.ReceivedContentMapper;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ReceivedContentRepositoryAdapter implements ReceivedContentRepositoryPort {

    private final ReceivedContentRepositoryJpa jpaRepository;
    private final ReceivedContentMapper mapper;

    @Override
    public ReceivedContent save(ReceivedContent content) {
        try {
            ReceivedContentJpa jpaEntity = mapper.toJpa(content);
            ReceivedContentJpa savedEntity = jpaRepository.saveAndFlush(jpaEntity);
            return mapper.toDomain(savedEntity);
        } catch (DataIntegrityViolationException e) {
            if (e.getRootCause() instanceof ConstraintViolationException cve && "uk_unit_sku".equals(cve.getConstraintName())) {
                throw AppException.of(ReceivingErrorCode.DUPLICATE_SKU_SCAN)
                        .with("sku", content.getSku());
            }
            throw e;
        }
    }

    @Override
    public List<ReceivedContent> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SkuQuantityDto> findActualSkuQuantitiesByReceiptId(UUID receiptId) {
        return jpaRepository.findActualSkuQuantitiesByReceiptId(receiptId);
    }
}