package com.waregang.receiving_service.integration;

import com.waregang.receiving_service.common.exception_handling.ProblemDetailSimpleFactory;
import com.waregang.receiving_service.integration.inventory_service.InventoryKafkaTestConsumer;
import com.waregang.receiving_service.integration.discreapncies_report.TestDiscrepanciesConsumer;
import com.waregang.receiving_service.security.application.JwtService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        JwtService.class,
        ProblemDetailSimpleFactory.class
})
public class IntegrationTestConfig {

    @Bean
    public InventoryKafkaTestConsumer inventoryKafkaTestConsumer() {
        return new InventoryKafkaTestConsumer();
    }

    @Bean
    public TestDiscrepanciesConsumer testDiscrepanciesConsumer() {
        return new TestDiscrepanciesConsumer();
    }
}
