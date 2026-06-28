package com.waregang.receiving_service.integration.discreapncies_report;

import com.waregang.receiving_service.inbound_delivery.infrastructure.InboundDeliveryRepository;
import com.waregang.receiving_service.integration.application.DiscrepanciesReportService;
import com.waregang.receiving_service.integration.infrastrusture.DiscrepanciesReportPort;
import com.waregang.receiving_service.receiving_process.domain.event.ClosedGoodsReceiptEvent;
import com.waregang.receiving_service.receiving_process.infrastructure.ReceivedContentRepository;
import com.waregang.receiving_service.integration.infrastrusture.dto.DiscrepanciesReport;
import com.waregang.receiving_service.integration.infrastrusture.dto.DiscrepancyLine;
import com.waregang.receiving_service.integration.infrastrusture.dto.DiscrepancyType;
import com.waregang.receiving_service.integration.infrastrusture.dto.SkuQuantityDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscrepanciesReportServiceTest {

    @Mock
    private DiscrepanciesReportPort port;

    @Mock
    private InboundDeliveryRepository deliveryRepository;

    @Mock
    private ReceivedContentRepository contentRepository;

    @InjectMocks
    private DiscrepanciesReportService service;

    private UUID receiptId;
    private UUID deliveryId;
    private ClosedGoodsReceiptEvent event;

    @BeforeEach
    void setUp() {
        receiptId = UUID.randomUUID();
        deliveryId = UUID.randomUUID();
        String gateNumber = "Gate1";
        event = new ClosedGoodsReceiptEvent(receiptId, deliveryId, gateNumber);
    }

    @Test
    @DisplayName("Should report SHORTAGE when actual quantity is less than expected")
    void shouldReportShortage() {
        when(deliveryRepository.findExpectedSkuQuantities(deliveryId))
                .thenReturn(List.of(skuQty("SKU-1", 100L)));
        when(contentRepository.findActualSkuQuantitiesByReceiptId(receiptId))
                .thenReturn(List.of(skuQty("SKU-1", 80L)));

        service.processClosedEvent(event);

        DiscrepanciesReport report = captureReport();
        assertThat(report.discrepancyLines()).hasSize(1);

        DiscrepancyLine line = report.discrepancyLines().getFirst();
        assertThat(line.sku()).isEqualTo("SKU-1");
        assertThat(line.expected()).isEqualTo(100L);
        assertThat(line.actual()).isEqualTo(80L);
        assertThat(line.type()).isEqualTo(DiscrepancyType.SHORTAGE);
    }

    @Test
    @DisplayName("Should report SURPLUS when actual quantity is more than expected")
    void shouldReportSurplus() {
        when(deliveryRepository.findExpectedSkuQuantities(deliveryId))
                .thenReturn(List.of(skuQty("SKU-1", 50L)));
        when(contentRepository.findActualSkuQuantitiesByReceiptId(receiptId))
                .thenReturn(List.of(skuQty("SKU-1", 60L)));

        service.processClosedEvent(event);

        DiscrepancyLine line = captureReport().discrepancyLines().getFirst();
        assertThat(line.type()).isEqualTo(DiscrepancyType.SURPLUS);
        assertThat(line.expected()).isEqualTo(50L);
        assertThat(line.actual()).isEqualTo(60L);
    }

    @Test
    @DisplayName("Should report SUBSTITUTION when SKU is in actual but not in expected")
    void shouldRecordSubstitution() {
        when(deliveryRepository.findExpectedSkuQuantities(deliveryId))
                .thenReturn(List.of(skuQty("SKU-1", 100L)));
        when(contentRepository.findActualSkuQuantitiesByReceiptId(receiptId))
                .thenReturn(List.of(skuQty("SKU-UNKNOWN", 10L)));

        service.processClosedEvent(event);

        List<DiscrepancyLine> lines = captureReport().discrepancyLines();
        assertThat(lines).hasSize(2);

        assertThat(lines).anySatisfy(line -> {
            assertThat(line.sku()).isEqualTo("SKU-UNKNOWN");
            assertThat(line.type()).isEqualTo(DiscrepancyType.SUBSTITUTION);
            assertThat(line.expected()).isEqualTo(0L);
            assertThat(line.actual()).isEqualTo(10L);
        });

        assertThat(lines).anySatisfy(line -> {
            assertThat(line.sku()).isEqualTo("SKU-1");
            assertThat(line.type()).isEqualTo(DiscrepancyType.SHORTAGE);
            assertThat(line.actual()).isEqualTo(0L);
        });
    }

    @Test
    @DisplayName("Should not send report when plan matches fact exactly")
    void shouldNotSendReportWhenNoDiscrepancies() {
        when(deliveryRepository.findExpectedSkuQuantities(deliveryId))
                .thenReturn(List.of(skuQty("SKU-1", 100L), skuQty("SKU-2", 50L)));
        when(contentRepository.findActualSkuQuantitiesByReceiptId(receiptId))
                .thenReturn(List.of(skuQty("SKU-1", 100L), skuQty("SKU-2", 50L)));

        service.processClosedEvent(event);

        verify(port, never()).sendReport(any());
    }

    @Test
    @DisplayName("Should report SHORTAGE for all SKUs when nothing was scanned")
    void shouldReportShortageForAllSkusWhenNothingScanned() {
        when(deliveryRepository.findExpectedSkuQuantities(deliveryId))
                .thenReturn(List.of(skuQty("SKU-1", 100L), skuQty("SKU-2", 50L)));
        when(contentRepository.findActualSkuQuantitiesByReceiptId(receiptId))
                .thenReturn(Collections.emptyList());

        service.processClosedEvent(event);

        List<DiscrepancyLine> lines = captureReport().discrepancyLines();
        assertThat(lines).hasSize(2);
        assertThat(lines).allMatch(l -> l.type() == DiscrepancyType.SHORTAGE);
        assertThat(lines).allMatch(l -> l.actual() == 0L);
    }

    @Test
    @DisplayName("Should not send report when no expected or actual quantities")
    void shouldNotSendReportWhenNoData() {
        when(deliveryRepository.findExpectedSkuQuantities(deliveryId)).thenReturn(Collections.emptyList());
        when(contentRepository.findActualSkuQuantitiesByReceiptId(receiptId)).thenReturn(Collections.emptyList());

        service.processClosedEvent(event);

        verify(port, never()).sendReport(any());
    }

    private DiscrepanciesReport captureReport() {
        ArgumentCaptor<DiscrepanciesReport> captor = ArgumentCaptor.forClass(DiscrepanciesReport.class);
        verify(port, times(1)).sendReport(captor.capture());
        return captor.getValue();
    }

    private SkuQuantityDto skuQty(String sku, long quantity) {
        return new SkuQuantityDto(sku, quantity);
    }
}
