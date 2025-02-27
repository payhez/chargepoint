package com.chargepoint.csms.transaction.controller;


import com.chargepoint.csms.lib.AuthenticationRequest;
import com.chargepoint.csms.transaction.controller.request.AuthorizationRequest;
import com.chargepoint.csms.transaction.controller.response.AuthorizationResponse;
import com.chargepoint.csms.transaction.enums.AuthorizationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import com.chargepoint.csms.transaction.util.JwtUtil;

import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping(value = "/authorize", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<?>> authorize(@RequestBody AuthorizationRequest request) {

        int identifierLength = request.getDriverIdentifier().getId().length();
        if (identifierLength < 20 || identifierLength > 80) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthorizationResponse(AuthorizationStatus.INVALID)));
        }

        String authToken = jwtUtil.generateToken(request.getDriverIdentifier().getId());

        AuthenticationRequest authRequest = AuthenticationRequest.builder()
                                                                .requestId(UUID.randomUUID())
                                                                .authenticationToken(authToken)
                                                                .build();



        //TODO Send token via kafka
        return Mono.just(ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new AuthorizationResponse(AuthorizationStatus.ACCEPTED)));
    }
}
