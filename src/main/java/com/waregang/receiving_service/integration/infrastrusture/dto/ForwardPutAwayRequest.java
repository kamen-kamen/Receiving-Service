package com.waregang.receiving_service.integration.infrastrusture.dto;

import com.waregang.receiving_service.receiving_process.domain.dto.ReceivedUnitDto;

import java.util.List;
import java.util.UUID;

public record ForwardPutAwayRequest(
        UUID workerSessionId,
        String timestamp,
        List<ReceivedUnitDto> receivedUnits
) {}
