package com.efcon.ride;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

@TestConfiguration
public class TestConfig {
    @Bean
    public NewTopic driverEventsTestTopic(@Value("${outbound.events.kafka.topic.driver-events}") String topicName) {
        return TopicBuilder.name(topicName)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
