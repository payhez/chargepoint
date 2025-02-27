package com.chargepoint.csms.transaction.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic authRequestsTopic() {
        return new NewTopic("authentication-requests", 3, (short) 1);
    }

    @Bean
    public NewTopic authResponsesTopic() {
        return new NewTopic("authentication-responses", 3, (short) 1);
    }
}