package com.waregang.receiving_service.inbound_delivery.api.dto;

import com.waregang.receiving_service.common.validation.ValidAsn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@ValidAsn // не проверяет на циклическую зависимость // только само наличие родителя
public record CreateDeliveryRequest(
        @NotBlank
        String externalId,

        @NotBlank
        String asnNumber,

        @NotBlank
        String warehouseId,

        @NotNull
        @Future
        LocalDateTime expectedArrivalDate,

        @NotEmpty
        List<@Valid CreateUnitRequest> unitRequests,

        @NotEmpty
        List<@Valid CreateContentRequest> contents
) {}
