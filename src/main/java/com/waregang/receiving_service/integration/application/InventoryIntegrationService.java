package com.waregang.receiving_service.integration.application;

import com.waregang.receiving_service.receiving_process.domain.event.WorkerSessionClosedEvent;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivedUnitJpa;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_repositories.ReceivedUnitRepositoryJpa;
import com.waregang.receiving_service.integration.infrastrusture.dto.ForwardPutAwayRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class InventoryIntegrationService {

    private final InventoryPutAwayPort putAwayPort;
    private final ReceivedUnitRepositoryJpa receivedUnitRepositoryJpa;
    private final PutAwayMapper putAwayMapper;

    @Transactional(readOnly = true)
    public void submitForPutAway(WorkerSessionClosedEvent event) {
        List<ReceivedUnitJpa> rootUnits = receivedUnitRepositoryJpa
                .findAllByWorkerSessionIdAndParentUnitIsNull(event.workerSessionId());

        if (CollectionUtils.isEmpty(rootUnits)) {
            log.warn("[PutAway] No root units found for workerSessionId={}", event.workerSessionId());
            return;
        }
        String occurredOn = Instant.now().toString();

        ForwardPutAwayRequest dto = putAwayMapper.toPutAwayRequestDto(
                rootUnits,
                event.workerSessionId(),
                occurredOn
        );

        putAwayPort.forwardForPutAway(dto);
    }
}
