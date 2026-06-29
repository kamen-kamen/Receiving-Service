package com.waregang.receiving_service.integration.discreapncies_report;

import com.waregang.receiving_service.BaseIT;
import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.fixtures.delivery.DeliveryMother;
import com.waregang.receiving_service.fixtures.user.UserMother;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.inbound_delivery.infrastructure.InboundDeliveryRepository;
import com.waregang.receiving_service.integration.infrastrusture.dto.DiscrepanciesReport;
import com.waregang.receiving_service.integration.infrastrusture.dto.DiscrepancyType;
import com.waregang.receiving_service.receiving_process.api.dto.ScanContentRequest;
import com.waregang.receiving_service.receiving_process.api.dto.ScanHandlingUnitRequest;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingRequest;
import com.waregang.receiving_service.receiving_process.application.GoodsReceiptService;
import com.waregang.receiving_service.receiving_process.application.ReceivingProcessService;
import com.waregang.receiving_service.security.User;
import com.waregang.receiving_service.security.UserPrincipal;
import com.waregang.receiving_service.security.UserRepository;
import com.waregang.receiving_service.security.api.dto.RegisterUserRequest;
import com.waregang.receiving_service.security.application.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

public class DiscrepanciesReportKafkaIT extends BaseIT {

    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;
    @Autowired private ReceivingProcessService receivingProcessService;
    @Autowired private GoodsReceiptService goodsReceiptService;
    @Autowired private InboundDeliveryRepository inboundDeliveryRepository;
    @Autowired private TestDiscrepanciesConsumer testConsumer;
    @Autowired private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        testConsumer.clear();
    }

    @Test
    @DisplayName("Should emit Discrepancy Event on SHORTAGE")
    void shouldEmitDiscrepancyEventOnShortage() {
        // 1. GIVEN
        InboundDelivery delivery = inboundDeliveryRepository.save(DeliveryMother.withNestedTree()); // Ожидается 100 SKU-123
        authService.registerBoxManager(new RegisterUserRequest("manager", delivery.getWarehouseId(), "manager@test.com", "password"));
        authService.registerBoxCat(new RegisterUserRequest("worker", delivery.getWarehouseId(), "worker@test.com", "password"));
        UserPrincipal manager = UserPrincipal.from(userRepository.findByEmail("manager@test.com").orElseThrow());
        UserPrincipal worker = UserPrincipal.from(userRepository.findByEmail("worker@test.com").orElseThrow());

        UUID receiptId = goodsReceiptService.startReceiving(
                new StartReceivingRequest(delivery.getAsnNumber(), "GATE-01"), manager
        ).receiptId();

        receivingProcessService.joinReceiving(worker, receiptId);
        receivingProcessService.scanHandlingUnit(new ScanHandlingUnitRequest("PALLET-01"), worker);
        receivingProcessService.scanHandlingUnit(new ScanHandlingUnitRequest("BOX-01"), worker);
        // Сканируем меньше, чем ожидалось
        receivingProcessService.scanContent(new ScanContentRequest("SKU-123", 50), worker);
        receivingProcessService.completeWorkerSession(worker);

        // 2. WHEN
        goodsReceiptService.closeReceiving(manager, receiptId);

        // 3. THEN
        await()
                .atMost(2, SECONDS)
                .untilAsserted(() -> {
                    var records = testConsumer.getRecords();
                    assertThat(records).isNotEmpty();

                    String payload = records.get(records.size() - 1).value();
                    DiscrepanciesReport report = jsonMapper.readValue(payload, DiscrepanciesReport.class);

                    assertThat(report.discrepancyLines()).hasSize(1);
                    var discrepancy = report.discrepancyLines().get(0);
                    assertThat(discrepancy.sku()).isEqualTo("SKU-123");
                    assertThat(discrepancy.expected()).isEqualTo(100);
                    assertThat(discrepancy.actual()).isEqualTo(50);
                    assertThat(discrepancy.type()).isEqualTo(DiscrepancyType.SHORTAGE);
                });
    }

    @Test
    @DisplayName("Should emit Discrepancy Event on SURPLUS")
    void shouldEmitDiscrepancyEventOnSurplus() {
        // 1. GIVEN
        InboundDelivery delivery = inboundDeliveryRepository.save(DeliveryMother.withNestedTree()); // Ожидается 100 SKU-123
        authService.registerBoxManager(new RegisterUserRequest("manager", delivery.getWarehouseId(), "manager2@test.com", "password"));
        authService.registerBoxCat(new RegisterUserRequest("worker", delivery.getWarehouseId(), "worker2@test.com", "password"));
        UserPrincipal manager = UserPrincipal.from(userRepository.findByEmail("manager2@test.com").orElseThrow());
        UserPrincipal worker = UserPrincipal.from(userRepository.findByEmail("worker2@test.com").orElseThrow());

        UUID receiptId = goodsReceiptService.startReceiving(
                new StartReceivingRequest(delivery.getAsnNumber(), "GATE-01"), manager
        ).receiptId();

        receivingProcessService.joinReceiving(worker, receiptId);
        receivingProcessService.scanHandlingUnit(new ScanHandlingUnitRequest("PALLET-01"), worker);
        receivingProcessService.scanHandlingUnit(new ScanHandlingUnitRequest("BOX-01"), worker);
        // Сканируем больше, чем ожидалось
        receivingProcessService.scanContent(new ScanContentRequest("SKU-123", 150), worker);
        receivingProcessService.completeWorkerSession(worker);

        // 2. WHEN
        goodsReceiptService.closeReceiving(manager, receiptId);

        // 3. THEN
        await()
                .atMost(2, SECONDS)
                .untilAsserted(() -> {
                    var records = testConsumer.getRecords();
                    assertThat(records).isNotEmpty();

                    String payload = records.get(records.size() - 1).value();
                    DiscrepanciesReport report = jsonMapper.readValue(payload, DiscrepanciesReport.class);

                    assertThat(report.discrepancyLines()).hasSize(1);
                    var discrepancy = report.discrepancyLines().get(0);
                    assertThat(discrepancy.sku()).isEqualTo("SKU-123");
                    assertThat(discrepancy.expected()).isEqualTo(100);
                    assertThat(discrepancy.actual()).isEqualTo(150);
                    assertThat(discrepancy.type()).isEqualTo(DiscrepancyType.SURPLUS);
                });
    }

    @Test
    @DisplayName("Should emit Discrepancy Event on SUBSTITUTION")
    void shouldEmitDiscrepancyEventOnSubstitution() {
        // 1. GIVEN
        InboundDelivery delivery = inboundDeliveryRepository.save(DeliveryMother.withNestedTree()); // Ожидается 100 SKU-123
        authService.registerBoxManager(new RegisterUserRequest("manager", delivery.getWarehouseId(), "manager3@test.com", "password"));
        authService.registerBoxCat(new RegisterUserRequest("worker", delivery.getWarehouseId(), "worker3@test.com", "password"));
        UserPrincipal manager = UserPrincipal.from(userRepository.findByEmail("manager3@test.com").orElseThrow());
        UserPrincipal worker = UserPrincipal.from(userRepository.findByEmail("worker3@test.com").orElseThrow());

        UUID receiptId = goodsReceiptService.startReceiving(
                new StartReceivingRequest(delivery.getAsnNumber(), "GATE-01"), manager
        ).receiptId();

        receivingProcessService.joinReceiving(worker, receiptId);
        receivingProcessService.scanHandlingUnit(new ScanHandlingUnitRequest("PALLET-01"), worker);
        receivingProcessService.scanHandlingUnit(new ScanHandlingUnitRequest("BOX-01"), worker);
        // Сканируем другой товар
        assertThatThrownBy(() -> receivingProcessService.scanContent(new ScanContentRequest("SKU-999", 20), worker))
                .isInstanceOf(AppException.class);
        receivingProcessService.completeWorkerSession(worker);

        // 2. WHEN
        goodsReceiptService.closeReceiving(manager, receiptId);

        // 3. THEN
        await()
                .atMost(2, SECONDS)
                .untilAsserted(() -> {
                    var records = testConsumer.getRecords();
                    assertThat(records).isNotEmpty();

                    String payload = records.get(records.size() - 1).value();
                    DiscrepanciesReport report = jsonMapper.readValue(payload, DiscrepanciesReport.class);

                    // Должно быть 1 расхождение: недостача
                    assertThat(report.discrepancyLines()).hasSize(1);

                    var shortage = report.discrepancyLines().stream()
                            .filter(d -> d.type() == DiscrepancyType.SHORTAGE)
                            .findFirst().orElseThrow();
                    assertThat(shortage.sku()).isEqualTo("SKU-123");
                    assertThat(shortage.expected()).isEqualTo(100);
                    assertThat(shortage.actual()).isZero();
                });
    }
}
