package com.waregang.receiving_service.inbound_delivery.infrastructure.adapters;

import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.inbound_delivery.domain.ports.InboundDeliveryRepositoryPort;
import com.waregang.receiving_service.inbound_delivery.infrastructure.InboundDeliveryRepositoryJpa;
import com.waregang.receiving_service.inbound_delivery.infrastructure.mappers.InboundDeliveryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class InboundDeliveryRepositoryAdapter implements InboundDeliveryRepositoryPort {

    private final InboundDeliveryRepositoryJpa jpaRepository;
    private final InboundDeliveryMapper mapper;

    @Override
    public InboundDelivery save(InboundDelivery delivery) {
        return mapper.toDomain(jpaRepository.save(mapper.toJpa(delivery)));
    }

    @Override
    public Optional<InboundDelivery> findByAsnNumber(String asn) {
        return jpaRepository.findByAsnNumber(asn).map(mapper::toDomain);
    }

    @Override
    public Optional<InboundDelivery> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}