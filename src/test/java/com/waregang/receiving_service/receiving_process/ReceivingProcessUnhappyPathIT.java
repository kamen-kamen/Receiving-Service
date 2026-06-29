package com.waregang.receiving_service.receiving_process;

import com.waregang.receiving_service.BaseIT;
import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.fixtures.delivery.DeliveryMother;
import com.waregang.receiving_service.fixtures.delivery.InboundDeliveryBuilder;
import com.waregang.receiving_service.fixtures.user.UserMother;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.inbound_delivery.infrastructure.InboundDeliveryRepository;
import com.waregang.receiving_service.receiving_process.api.dto.ScanHandlingUnitRequest;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingRequest;
import com.waregang.receiving_service.receiving_process.application.GoodsReceiptService;
import com.waregang.receiving_service.receiving_process.application.ReceivingProcessService;
import com.waregang.receiving_service.receiving_process.infrastructure.GoodsReceiptRepository;
import com.waregang.receiving_service.receiving_process.infrastructure.WorkerReceivingSessionRepository;
import com.waregang.receiving_service.security.User;
import com.waregang.receiving_service.security.UserPrincipal;
import com.waregang.receiving_service.security.UserRepository;
import com.waregang.receiving_service.security.api.dto.RegisterUserRequest;
import com.waregang.receiving_service.security.application.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ReceivingProcessUnhappyPathIT extends BaseIT {

    @Autowired
    private ReceivingProcessService receivingProcessService;

    @Autowired
    private GoodsReceiptService goodsReceiptService;

    @Autowired
    private InboundDeliveryRepository inboundDeliveryRepository;

    @Autowired
    private GoodsReceiptRepository goodsReceiptRepository;

    @Autowired
    private WorkerReceivingSessionRepository sessionRepository;

    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;

    // =============================================
    // 1. Воркер пытается join к уже закрытой приёмке
    // =============================================
    @Test
    @DisplayName("Should throw RECEIPT_NOT_FOUND when worker tries to join closed receipt")
    void shouldFailWhenWorkerJoinsClosedReceipt() {
        // Given
        InboundDelivery delivery = DeliveryMother.withNestedTree();
        inboundDeliveryRepository.save(delivery);
        authService.registerBoxManager(new RegisterUserRequest("manager4", delivery.getWarehouseId(), "manager4@test.com", "password"));
        authService.registerBoxCat(new RegisterUserRequest("worker4", delivery.getWarehouseId(), "worker4@test.com", "password"));
        UserPrincipal manager = UserPrincipal.from(userRepository.findByEmail("manager4@test.com").orElseThrow());
        UserPrincipal worker = UserPrincipal.from(userRepository.findByEmail("worker4@test.com").orElseThrow());

        var startResponse = goodsReceiptService.startReceiving(
                new StartReceivingRequest(delivery.getAsnNumber(), "GATE-01"), manager
        );
        UUID receiptId = startResponse.receiptId();

        // Закрываем приёмку сразу
        goodsReceiptService.closeReceiving(manager, receiptId);

        // When / Then
        assertThatThrownBy(() -> receivingProcessService.joinReceiving(worker, receiptId))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ReceivingErrorCode.RECEIPT_INVALID_STATE);
                });
    }

    // =============================================
    // 2. Воркер уже в сессии пытается join снова
    // =============================================
    @Test
    @DisplayName("Should throw WORKER_ALREADY_JOINED when worker tries to join second receipt")
    void shouldFailWhenWorkerAlreadyInSession() {
        // Given
        InboundDelivery delivery1 = DeliveryMother.withNestedTree();
        InboundDelivery delivery2 = InboundDeliveryBuilder.aDelivery()
                .withAsn("ASN-SECOND-01")
                .build();
        inboundDeliveryRepository.save(delivery1);
        inboundDeliveryRepository.save(delivery2);
        authService.registerBoxManager(new RegisterUserRequest("manager5", delivery1.getWarehouseId(), "manager5@test.com", "password"));
        authService.registerBoxCat(new RegisterUserRequest("worker5", delivery1.getWarehouseId(), "worker5@test.com", "password"));
        UserPrincipal manager = UserPrincipal.from(userRepository.findByEmail("manager5@test.com").orElseThrow());
        UserPrincipal worker = UserPrincipal.from(userRepository.findByEmail("worker5@test.com").orElseThrow());

        UUID receiptId1 = goodsReceiptService.startReceiving(
                new StartReceivingRequest(delivery1.getAsnNumber(), "GATE-01"), manager
        ).receiptId();

        UUID receiptId2 = goodsReceiptService.startReceiving(
                new StartReceivingRequest(delivery2.getAsnNumber(), "GATE-02"), manager
        ).receiptId();

        // Воркер уже присоединился к первой приёмке
        receivingProcessService.joinReceiving(worker, receiptId1);

        // When / Then — пытается присоединиться ко второй
        assertThatThrownBy(() -> receivingProcessService.joinReceiving(worker, receiptId2))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ReceivingErrorCode.WORKER_ALREADY_JOINED);
                });
    }

    // =============================================
    // 3. Закрытие приёмки с активными сессиями
    // =============================================
    @Test
    @DisplayName("Should throw RECEIPT_INVALID_STATE when closing receipt with active worker sessions")
    void shouldFailWhenClosingReceiptWithActiveSessions() {
        // Given
        InboundDelivery delivery = DeliveryMother.withNestedTree();
        inboundDeliveryRepository.save(delivery);
        authService.registerBoxManager(new RegisterUserRequest("manager6", delivery.getWarehouseId(), "manager6@test.com", "password"));
        authService.registerBoxCat(new RegisterUserRequest("worker6", delivery.getWarehouseId(), "worker6@test.com", "password"));
        UserPrincipal manager = UserPrincipal.from(userRepository.findByEmail("manager6@test.com").orElseThrow());
        UserPrincipal worker = UserPrincipal.from(userRepository.findByEmail("worker6@test.com").orElseThrow());

        UUID receiptId = goodsReceiptService.startReceiving(
                new StartReceivingRequest(delivery.getAsnNumber(), "GATE-01"), manager
        ).receiptId();

        // Воркер присоединился но не закрыл сессию
        receivingProcessService.joinReceiving(worker, receiptId);

        // When / Then
        assertThatThrownBy(() -> goodsReceiptService.closeReceiving(manager, receiptId))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ReceivingErrorCode.RECEIPT_INVALID_STATE);
                });
    }

    // =============================================
    // 4. Скан LPN которого нет в ASN
    // =============================================
    @Test
    @DisplayName("Should throw SCANNED_HU_IS_UNKNOWN when scanning LPN not in ASN")
    void shouldFailWhenScanningUnknownLpn() {
        // Given
        InboundDelivery delivery = DeliveryMother.withNestedTree();
        inboundDeliveryRepository.save(delivery);
        authService.registerBoxManager(new RegisterUserRequest("manager7", delivery.getWarehouseId(), "manager7@test.com", "password"));
        authService.registerBoxCat(new RegisterUserRequest("worker7", delivery.getWarehouseId(), "worker7@test.com", "password"));
        UserPrincipal manager = UserPrincipal.from(userRepository.findByEmail("manager7@test.com").orElseThrow());
        UserPrincipal worker = UserPrincipal.from(userRepository.findByEmail("worker7@test.com").orElseThrow());

        UUID receiptId = goodsReceiptService.startReceiving(
                new StartReceivingRequest(delivery.getAsnNumber(), "GATE-01"), manager
        ).receiptId();

        receivingProcessService.joinReceiving(worker, receiptId);

        // When / Then — сканируем LPN которого нет в ASN
        assertThatThrownBy(() -> receivingProcessService.scanHandlingUnit(
                new ScanHandlingUnitRequest("UNKNOWN-LPN-999"), worker
        ))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ReceivingErrorCode.SCANNED_HU_NOT_IN_THE_DELIVERY);
                });
    }
}
