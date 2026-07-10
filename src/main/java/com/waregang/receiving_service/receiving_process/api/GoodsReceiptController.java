package com.waregang.receiving_service.receiving_process.api;

import com.waregang.receiving_service.common.idempotency.Idempotent;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceiptStatus;
import com.waregang.receiving_service.security.UserPrincipal;
import com.waregang.receiving_service.receiving_process.api.dto.GetOpenedReceiptsResponse;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingRequest;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingResponse;
import com.waregang.receiving_service.receiving_process.application.GoodsReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor

@RestController
@RequestMapping("/api/goods-receipts")
public class GoodsReceiptController {
    private final GoodsReceiptService service;

    @Idempotent
    @PostMapping("/open")
    @PreAuthorize("hasAuthority('BOX_MANAGER')")
    public ResponseEntity<StartReceivingResponse> startReceiving(
            @Valid @RequestBody StartReceivingRequest request,
            @AuthenticationPrincipal UserPrincipal manager
    ) {
        StartReceivingResponse response = service.startReceiving(request, manager);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{receipt-id}/close")
    @PreAuthorize("hasAuthority('BOX_MANAGER')")
    public ResponseEntity<Void> closeReceiving(
            @AuthenticationPrincipal UserPrincipal manager,
            @PathVariable(value = "receipt-id") UUID receiptId
    ) {
        service.closeReceiving(manager, receiptId);

        return ResponseEntity.ok().build();
    }


    @GetMapping
    public ResponseEntity<GetOpenedReceiptsResponse> getOpenedGoodsReceipts(
            @RequestParam GoodsReceiptStatus receiptStatus,
            @AuthenticationPrincipal UserPrincipal worker
    ) {
        GetOpenedReceiptsResponse response = service.findAllByStatus(worker, receiptStatus);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}