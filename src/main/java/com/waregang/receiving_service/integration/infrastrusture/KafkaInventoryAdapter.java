package com.waregang.receiving_service.integration.infrastrusture;

import com.waregang.receiving_service.integration.infrastrusture.dto.ForwardPutAwayRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaInventoryAdapter implements InventoryPutAwayPort {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "inventory-put-away";

    @Override
    public void forwardForPutAway(ForwardPutAwayRequest request) {
        String key = request.workerSessionId().toString();

        kafkaTemplate.send(TOPIC, key, request)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] Failed to send put-away event. topic={}, key={}, error={}",
                                TOPIC, key, ex.getMessage());
                    } else {
                        log.info("[Kafka] Put-away event sent. topic={}, partition={}, offset={}",
                                TOPIC,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
