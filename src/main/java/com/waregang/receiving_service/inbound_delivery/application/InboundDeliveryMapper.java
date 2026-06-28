package com.waregang.receiving_service.inbound_delivery.application;

import com.waregang.receiving_service.inbound_delivery.api.dto.CreateContentRequest;
import com.waregang.receiving_service.inbound_delivery.api.dto.CreateDeliveryRequest;
import com.waregang.receiving_service.inbound_delivery.api.dto.CreateUnitRequest;
import com.waregang.receiving_service.inbound_delivery.domain.model.HandlingUnit;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InboundDeliveryMapper {

    public InboundDelivery toEntity(CreateDeliveryRequest request) {
        InboundDelivery delivery = InboundDelivery.create(
                request.externalId(),
                request.asnNumber(),
                request.warehouseId()
        );

        Map<String, List<CreateUnitRequest>> unitsByParentLpn = request.unitRequests().stream()
                .filter(r -> r.parentLpn() != null)
                .collect(Collectors.groupingBy(CreateUnitRequest::parentLpn));

        Map<String, List<CreateContentRequest>> contentsByParentLpn = request.contents().stream()
                .collect(Collectors.groupingBy(CreateContentRequest::parentLpn));

        List<CreateUnitRequest> rootUnitRequests = request.unitRequests().stream()
                .filter(r -> r.parentLpn() == null)
                .toList();


        // 4. Запускаем рекурсивную сборку дерева, начиная с корневых элементов
        rootUnitRequests.forEach(rootRequest -> {
            HandlingUnit rootUnit = createUnitRecursively(
                    rootRequest,
                    delivery, // Передаем поставку
                    unitsByParentLpn,
                    contentsByParentLpn
            );
            // Используем правильный helper-метод для добавления в коллекцию
            delivery.addHandlingUnit(rootUnit);
        });

        return delivery;
    }

    private HandlingUnit createUnitRecursively(
            CreateUnitRequest currentRequest,
            InboundDelivery delivery,
            Map<String, List<CreateUnitRequest>> unitsByParent,
            Map<String, List<CreateContentRequest>> contentsByParent
    ) {
        // Создаем текущий HandlingUnit, сразу связывая его с поставкой
        HandlingUnit currentUnit = HandlingUnit.create(currentRequest.lpn(), delivery);

        // Добавляем вложенный контент, используя правильное имя метода
        contentsByParent.getOrDefault(currentRequest.lpn(), Collections.emptyList())
                .forEach(contentReq -> currentUnit.fillWithContent(contentReq.sku(), contentReq.quantity()));

        // Рекурсивно создаем и добавляем дочерние HandlingUnits
        unitsByParent.getOrDefault(currentRequest.lpn(), Collections.emptyList())
                .forEach(childRequest -> {
                    HandlingUnit childUnit = createUnitRecursively(
                            childRequest,
                            delivery,
                            unitsByParent,
                            contentsByParent
                    );
                    currentUnit.addChild(childUnit);
                });

        return currentUnit;
    }
}
