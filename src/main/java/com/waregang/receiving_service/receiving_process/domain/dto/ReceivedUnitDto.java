package com.waregang.receiving_service.receiving_process.domain.dto;

import java.util.List;

public record ReceivedUnitDto(
        String lpn,
        List<ReceivedUnitDto> childUnits,
        List<ReceivedContentDto> contents
) {}
