package com.waregang.receiving_service.inbound_delivery.infrastructure.adapters;

import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.InboundDeliveryErrorCode;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.inbound_delivery.domain.ports.InboundDeliveryRepositoryPort;
import com.waregang.receiving_service.inbound_delivery.infrastructure.jpa_repositories.InboundDeliveryRepositoryJpa;
import com.waregang.receiving_service.inbound_delivery.infrastructure.jpa_entities.InboundDeliveryJpa;
import com.waregang.receiving_service.inbound_delivery.infrastructure.mappers.InboundDeliveryMapper;
import com.waregang.receiving_service.integration.infrastrusture.dto.SkuQuantityDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Override
    public InboundDelivery update(InboundDelivery delivery) {
        InboundDeliveryJpa deliveryJpa = jpaRepository.findById(delivery.getId())
                .orElseThrow(() -> AppException.of(InboundDeliveryErrorCode.DELIVERY_NOT_FOUND)
                        .with("inbound_delivery_id", delivery.getId()));

        mapper.updateJpaFromDomain(deliveryJpa, delivery);

        return mapper.toDomain(deliveryJpa);
    }

    @Override
    public List<SkuQuantityDto> findExpectedSkuQuantities(UUID deliveryId) {
        return jpaRepository.findExpectedSkuQuantities(deliveryId);
    }
}