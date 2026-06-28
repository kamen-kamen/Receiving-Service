package com.waregang.receiving_service.receiving_process.api.dto;

import java.util.UUID;

public record JoinReceivingResponse(
        UUID workerSessionId
) {}
