package com.waregang.receiving_service.receiving_process.infrastructure.dto;

import org.jspecify.annotations.Nullable;
import java.util.List;

public record ReceivedUnitDto(
        String lpn,
        List<ReceivedUnitDto> childUnits,
        List<ReceivedContentDto> contents
) {}
