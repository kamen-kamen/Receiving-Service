package com.waregang.receiving_service.integration.inventory_service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@TestComponent
public class InventoryKafkaTestConsumer {
    // Use a CopyOnWriteArrayList so Awaitility can iterate while Kafka adds to it
    private final List<ConsumerRecord<String, String>> records = new CopyOnWriteArrayList<>();

    @KafkaListener(topics = "inventory-put-away", groupId = "test-group")
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