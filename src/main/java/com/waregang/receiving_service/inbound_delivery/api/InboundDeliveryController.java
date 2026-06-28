package com.waregang.receiving_service.inbound_delivery.api;


import com.waregang.receiving_service.inbound_delivery.application.InboundDeliveryService;
import com.waregang.receiving_service.inbound_delivery.api.dto.CreateDeliveryRequest;
import com.waregang.receiving_service.inbound_delivery.api.dto.CreateDeliveryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor

@RestController
@RequestMapping("/api/inbound_deliveries")
public class InboundDeliveryController {
    private final InboundDeliveryService service;

    @PostMapping
    public ResponseEntity<CreateDeliveryResponse> createInboundDelivery(
            @Valid @RequestBody CreateDeliveryRequest request
    ) {
        CreateDeliveryResponse response = service.createDelivery(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

