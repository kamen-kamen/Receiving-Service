package com.waregang.receiving_service.receiving_process.infrastructure.adapters;

import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivedUnit;
import com.waregang.receiving_service.receiving_process.domain.ports.ReceivedUnitRepositoryPort;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.ReceivedUnitJpa;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_repositories.ReceivedUnitRepositoryJpa;
import com.waregang.receiving_service.receiving_process.infrastructure.mappers.ReceivedUnitMapper;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ReceivedUnitRepositoryAdapter implements ReceivedUnitRepositoryPort {

    private final ReceivedUnitRepositoryJpa jpaRepository;
    private final ReceivedUnitMapper mapper;

    @Override
    public ReceivedUnit save(ReceivedUnit unit) {
        try {
            ReceivedUnitJpa jpaEntity = mapper.toJpa(unit);
            ReceivedUnitJpa savedEntity = jpaRepository.saveAndFlush(jpaEntity);
            return mapper.toDomain(savedEntity);
        } catch (DataIntegrityViolationException e) {
            if (e.getRootCause() instanceof ConstraintViolationException cve && "uk_receipt_lpn".equals(cve.getConstraintName())) {
                throw AppException.of(ReceivingErrorCode.LPN_ALREADY_SCANNED)
                        .with("lpn", unit.getLpn())
                        .with("receiptId", unit.getReceiptId());
            }
            throw e;
        }
    }

    @Override
    public Optional<ReceivedUnit> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<ReceivedUnit> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReceivedUnit> findAllRootUnitsByWorkerSessionId(UUID workerSessionId) {
        return jpaRepository.findAllByWorkerSessionIdAndParentUnitIsNull(workerSessionId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}