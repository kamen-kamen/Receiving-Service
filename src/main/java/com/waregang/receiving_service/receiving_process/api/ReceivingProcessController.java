package com.waregang.receiving_service.receiving_process.api;

import com.waregang.receiving_service.security.UserPrincipal;
import com.waregang.receiving_service.receiving_process.api.dto.*;
import com.waregang.receiving_service.receiving_process.application.ReceivingProcessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor

@RestController
@RequestMapping("/api/receiving-sessions")
public class ReceivingProcessController {
    private final ReceivingProcessService service;

    @PostMapping("/{receiptId}/join")
    public ResponseEntity<JoinReceivingResponse> joinReceiving(
            @PathVariable("receiptId") UUID receiptId,
            @AuthenticationPrincipal UserPrincipal worker
    ) {
        JoinReceivingResponse response = service.joinReceiving(worker, receiptId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/scans/handling-units")
    public ResponseEntity<ScanHandlingUnitResponse> scanHandlingUnit(
            @Valid @RequestBody ScanHandlingUnitRequest request,
            @AuthenticationPrincipal UserPrincipal worker
    ) {
        ScanHandlingUnitResponse response = service.scanHandlingUnit(request, worker);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/scans/handling-units/content")
    public ResponseEntity<ScanContentResponse> receiveContent(
            @Valid @RequestBody ScanContentRequest request,
            @AuthenticationPrincipal UserPrincipal worker
    ) {
        ScanContentResponse response = service.scanContent(request, worker);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/handling-units/navigate-back")
    public ResponseEntity<NavigationBackResponse> getBackToPreviousUnit(
            @AuthenticationPrincipal UserPrincipal worker
    ) {
        NavigationBackResponse response = service.getBackToPreviousUnit(worker);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/worker-sessions/complete")
    public ResponseEntity<Void> completeWorkerSession(
            @AuthenticationPrincipal UserPrincipal worker
    ) {
        service.completeWorkerSession(worker);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
