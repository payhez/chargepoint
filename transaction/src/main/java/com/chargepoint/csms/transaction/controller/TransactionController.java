package com.chargepoint.csms.transaction.controller;


import com.chargepoint.csms.transaction.controller.request.AuthorizationRequest;
import com.chargepoint.csms.transaction.controller.response.AuthorizationResponse;
import com.chargepoint.csms.transaction.enums.AuthorizationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping("/transaction")
public class TransactionController {

    @PostMapping(value = "/authorize", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<?>> authorize(@RequestBody AuthorizationRequest request) {
        return Mono.just(ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new AuthorizationResponse(AuthorizationStatus.ACCEPTED)));
    }
}
