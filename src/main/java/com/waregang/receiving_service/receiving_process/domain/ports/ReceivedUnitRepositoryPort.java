package com.waregang.receiving_service.receiving_process.domain.ports;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivedUnit;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReceivedUnitRepositoryPort {
    ReceivedUnit save(ReceivedUnit unit);
    Optional<ReceivedUnit> findById(UUID id);
    List<ReceivedUnit> findAll();
}