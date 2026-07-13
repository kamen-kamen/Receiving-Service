package com.waregang.receiving_service.integration.application;

import com.waregang.receiving_service.inbound_delivery.domain.ports.InboundDeliveryRepositoryPort;
import com.waregang.receiving_service.receiving_process.domain.event.ClosedGoodsReceiptEvent;
import com.waregang.receiving_service.receiving_process.domain.ports.ReceivedContentRepositoryPort;
import com.waregang.receiving_service.integration.infrastrusture.dto.DiscrepanciesReport;
import com.waregang.receiving_service.integration.infrastrusture.dto.DiscrepancyLine;
import com.waregang.receiving_service.integration.infrastrusture.dto.DiscrepancyType;
import com.waregang.receiving_service.integration.infrastrusture.dto.SkuQuantityDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor

@Service
public class DiscrepanciesReportService {
    private final DiscrepanciesReportPort port;
    private final InboundDeliveryRepositoryPort deliveryRepository;
    private final ReceivedContentRepositoryPort contentRepository;

    @Transactional(readOnly = true)
    public void processClosedEvent(ClosedGoodsReceiptEvent event) {
        final List<DiscrepancyLine> discrepancies = new ArrayList<>();

        Map<String, Long> actual = contentRepository
                .findActualSkuQuantitiesByReceiptId(event.receiptId())
                .stream()
                .collect(Collectors.toMap(SkuQuantityDto::sku, SkuQuantityDto::quantity));

        Map<String, Long> expected = deliveryRepository
                .findExpectedSkuQuantities(event.inboundDeliveryId())
                .stream()
                .collect(Collectors.toMap(SkuQuantityDto::sku, SkuQuantityDto::quantity));

        Set<String> keySet = new HashSet<>(expected.keySet());
        keySet.addAll(actual.keySet());

        keySet.forEach(sku -> {
            long expectedQty = expected.getOrDefault(sku, 0L);
            long actualQty = actual.getOrDefault(sku, 0L);

            if (isMissing(expectedQty, actualQty))
                discrepancies.add(
                        recordDiscrepancy(sku, expectedQty, actualQty)
                );
            else if (isSubstitution(expectedQty, actualQty))
                discrepancies.add(
                        recordSubstitution(sku, expectedQty, actualQty)
                );
            else if (expectedQty != actualQty)
                discrepancies.add(
                        recordDiscrepancy(sku, expectedQty, actualQty)
                );

        });

        if (!discrepancies.isEmpty()) {
            port.sendReport(new DiscrepanciesReport(
                    event.inboundDeliveryId(),
                    event.receiptId(),
                    discrepancies
            ));
        }
    }

    private DiscrepancyLine recordSubstitution(String sku, long expected, long actual) {
        return new DiscrepancyLine(
                sku,
                expected,
                actual,
                DiscrepancyType.SUBSTITUTION
        );
    }

    private DiscrepancyLine recordDiscrepancy(String sku, long expected, long actual) {
        DiscrepancyType type = (expected - actual) > 0 ? DiscrepancyType.SHORTAGE : DiscrepancyType.SURPLUS;

        return new DiscrepancyLine(
                sku,
                expected,
                actual,
                type
        );
    }

    private boolean isMissing(long expected, long actual) {
        return expected > 0 && actual == 0;
    }

    private boolean isSubstitution(long expected, long actual) {
        return expected == 0 && actual > 0;
    }
}