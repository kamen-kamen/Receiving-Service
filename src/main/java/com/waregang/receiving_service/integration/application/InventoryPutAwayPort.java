package com.waregang.receiving_service.integration.application;

import com.waregang.receiving_service.integration.infrastrusture.dto.ForwardPutAwayRequest;

public interface InventoryPutAwayPort {
    void forwardForPutAway(ForwardPutAwayRequest dto);
}
