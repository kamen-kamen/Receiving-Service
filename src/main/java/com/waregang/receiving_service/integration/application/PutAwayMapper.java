package com.waregang.receiving_service.integration.application;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivedContent;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivedUnit;
import com.waregang.receiving_service.receiving_process.infrastructure.dto.ReceivedContentDto;
import com.waregang.receiving_service.integration.infrastrusture.dto.ForwardPutAwayRequest;
import com.waregang.receiving_service.receiving_process.infrastructure.dto.ReceivedUnitDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PutAwayMapper {

    public ForwardPutAwayRequest toPutAwayRequestDto(
            List<ReceivedUnit> rootUnits,
            UUID workerSessionId,
            String occurredOn
    ) {
        // Проверка на пустой список теперь находится в сервисе, здесь она не нужна.
        List<ReceivedUnitDto> rootUnitDtos = rootUnits.stream()
                .map(this::toReceivedUnitDto)
                .collect(Collectors.toList());

        return new ForwardPutAwayRequest(
                workerSessionId,
                occurredOn,
                rootUnitDtos);
    }

    private ReceivedUnitDto toReceivedUnitDto(ReceivedUnit unit) {
        // Рекурсивно мапим дочерние HU
        List<ReceivedUnitDto> childUnitDtos = unit.getChildUnits().stream()
                .map(this::toReceivedUnitDto)
                .collect(Collectors.toList());

        // Мапим вложенный контент, передавая LPN родителя, чтобы избежать лишнего SELECT
        List<ReceivedContentDto> contentDtos = unit.getContents().stream()
                .map(content -> toReceivedContentDto(content, unit.getLpn()))
                .collect(Collectors.toList());

        return new ReceivedUnitDto(
                unit.getLpn(),
                childUnitDtos,
                contentDtos
        );
    }

    private ReceivedContentDto toReceivedContentDto(ReceivedContent content, String containerLpn) {
        return new ReceivedContentDto(
                containerLpn, // Используем LPN, который уже есть в памяти
                content.getSku(),
                content.getQuantity()
        );
    }
}
