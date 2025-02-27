package com.chargepoint.csms.transaction.service;

import com.chargepoint.csms.lib.AuthenticationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String, AuthenticationRequest> kafkaTemplate;

    public void sendAuthRequest(AuthenticationRequest request) {
        kafkaTemplate.send("authentication-requests", request);
    }
}