package com.waregang.receiving_service.integration.infrastrusture;

import com.waregang.receiving_service.integration.application.DiscrepanciesReportPort;
import com.waregang.receiving_service.integration.infrastrusture.dto.DiscrepanciesReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class KafkaDiscrepanciesReportAdapter implements DiscrepanciesReportPort {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "discrepancies-report";

    @Override
    public void sendReport(DiscrepanciesReport report) {
        String key = report.inboundDeliveryId().toString();
        log.info("[Kafka] Sending discrepancies report. topic={}, key={}", TOPIC, key);

        kafkaTemplate.send(TOPIC, key, report)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] Failed to send discrepancies report. topic={}, key={}, error={}",
                                TOPIC, key, ex.getMessage());
                    } else {
                        log.info("[Kafka] Discrepancies report sent. topic={}, partition={}, offset={}",
                                TOPIC,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
