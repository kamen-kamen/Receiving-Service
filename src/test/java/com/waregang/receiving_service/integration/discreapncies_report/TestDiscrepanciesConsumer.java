package com.waregang.receiving_service.integration.discreapncies_report;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@TestComponent
public class TestDiscrepanciesConsumer {
    private final List<ConsumerRecord<String, String>> records = new CopyOnWriteArrayList<>();

    @KafkaListener(topics = "discrepancies-report", groupId = "test-discrepancies-group")
    public void receive(ConsumerRecord<String, String> record) {
        records.add(record);
    }

    public List<ConsumerRecord<String, String>> getRecords() {
        return Collections.unmodifiableList(records);
    }

    public void clear() {
        records.clear();
    }
}
