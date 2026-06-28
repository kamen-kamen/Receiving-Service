package com.waregang.receiving_service.receiving_process.domain.event;

import java.util.UUID;

public record WorkerSessionClosedEvent(
        UUID workerSessionId
) {}
