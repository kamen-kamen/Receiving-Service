package com.waregang.receiving_service.integration.inventory_service;

import com.waregang.receiving_service.BaseIT;
import com.waregang.receiving_service.fixtures.delivery.DeliveryMother;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.inbound_delivery.infrastructure.InboundDeliveryRepository;
import com.waregang.receiving_service.integration.infrastrusture.dto.ForwardPutAwayRequest;
import com.waregang.receiving_service.receiving_process.api.dto.ScanContentRequest;
import com.waregang.receiving_service.receiving_process.api.dto.ScanHandlingUnitRequest;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingRequest;
import com.waregang.receiving_service.receiving_process.application.GoodsReceiptService;
import com.waregang.receiving_service.receiving_process.application.ReceivingProcessService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

class InventoryKafkaAdapterIT extends BaseIT {

    @Autowired private ReceivingProcessService receivingProcessService;
    @Autowired private GoodsReceiptService goodsReceiptService;
    @Autowired private InboundDeliveryRepository inboundDeliveryRepository;
    @Autowired private InventoryKafkaTestConsumer testConsumer;
    @Autowired private JsonMapper jsonMapper; // Для десериализации ответа из Kafka

    @BeforeEach
    void setUp() {
        testConsumer.clear();
    }

    @Test
    @DisplayName("Should emit correct ForwardPutAwayEvent when receipt is closed")
    void shouldEmitIntegrationEventWithCorrectData() {
        // 1. GIVEN: Подготовка данных через Mother
        InboundDelivery delivery = inboundDeliveryRepository.save(DeliveryMother.withNestedTree());

        // Проходим минимальный цикл приемки
        UUID receiptId = goodsReceiptService.startReceiving(
                new StartReceivingRequest(delivery.getAsnNumber(), "GATE-01"), managerPrincipal
        ).receiptId();

        var workerSessionId = receivingProcessService.joinReceiving(workerPrincipal, receiptId).workerSessionId();
        receivingProcessService.scanHandlingUnit(new ScanHandlingUnitRequest("PALLET-01"), workerPrincipal);
        receivingProcessService.scanHandlingUnit(new ScanHandlingUnitRequest("BOX-01"), workerPrincipal);
        receivingProcessService.scanContent(new ScanContentRequest("SKU-123", 100), workerPrincipal);
        receivingProcessService.completeWorkerSession(workerPrincipal);

        // 2. WHEN: Триггер события — закрытие приемки менеджером
        goodsReceiptService.closeReceiving(managerPrincipal, receiptId);

    // 3. THEN:
        await()
                .atMost(1, SECONDS)
                .untilAsserted(() -> {
                    var records = testConsumer.getRecords();

                    assertThat(records)
                            .withFailMessage("No messages received in Kafka topic")
                            .isNotEmpty();

                    ConsumerRecord<String, String> lastRecord = records.get(records.size() - 1);

                    ForwardPutAwayRequest event = jsonMapper.readValue(lastRecord.value(), ForwardPutAwayRequest.class);

                    assertThat(event.workerSessionId()).isEqualTo(workerSessionId);});
    }
}