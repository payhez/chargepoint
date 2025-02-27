package com.chargepoint.csms.authentication.service;

import com.chargepoint.csms.authentication.entity.Driver;
import com.chargepoint.csms.authentication.repository.DriverRepository;
import com.chargepoint.csms.lib.AuthenticationRequest;
import com.chargepoint.csms.lib.AuthenticationResponse;
import com.chargepoint.csms.lib.AuthorizationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class AuthenticationService {

    @Autowired
    private KafkaTemplate<String, AuthenticationResponse> kafkaTemplate;

    @Autowired
    private TokenValidator tokenValidator;

    @Autowired
    private DriverRepository driverRepository;

    private static final BigDecimal CHARGING_FEE = BigDecimal.valueOf(175.25);

    @KafkaListener(topics = "authentication-requests")
    public void handleAuthRequest(AuthenticationRequest request) {
        AuthorizationStatus authorizationStatus;

        String identifier = tokenValidator.extractIdentifier(request.getAuthenticationToken());

        if (identifier == null) {
            authorizationStatus = AuthorizationStatus.INTERNAL_ERROR;
        } else {
            authorizationStatus = checkDriverAndCharge(identifier).block();
        }
        AuthenticationResponse response = AuthenticationResponse.builder()
                                                .requestId(request.getRequestId())
                                                .status(authorizationStatus)
                                                .build();

        kafkaTemplate.send("authentication-responses", response);
    }

    private Mono<AuthorizationStatus> checkDriverAndCharge(final String driverIdentifier) {
        return driverRepository.findById(driverIdentifier)
                .flatMap(driver -> {
                    if (driver.getCredit().compareTo(CHARGING_FEE) < 0) {
                        return Mono.just(AuthorizationStatus.REJECTED);
                    }
                    driver.setCredit(driver.getCredit().subtract(CHARGING_FEE));
                    return driverRepository.save(driver)
                            .thenReturn(AuthorizationStatus.ACCEPTED);
                }).defaultIfEmpty(AuthorizationStatus.UNKNOWN);
    }
}
