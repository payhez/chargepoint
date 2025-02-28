package com.chargepoint.csms.transaction.controller;


import com.chargepoint.csms.lib.AuthenticationRequest;
import com.chargepoint.csms.lib.AuthenticationResponse;
import com.chargepoint.csms.lib.AuthorizationStatus;
import com.chargepoint.csms.transaction.controller.request.AuthorizationRequest;
import com.chargepoint.csms.transaction.controller.response.AuthorizationResponse;
import com.chargepoint.csms.transaction.service.KafkaProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.chargepoint.csms.transaction.util.JwtUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@RestController
@Slf4j
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private KafkaProducer kafkaProducer;

    /**
     * A cache of to keep track of pending requests to Authentication service.
     *
     * <p>This map stores the asynchronous authentication responses that WILL be received from Authentication service.
     * Each key is a {@link UUID} representing the randomly generated uuid of the authentication requests.
     * <ul>
     *   <li>The key is a randomly generated {@link UUID} to help matching the requests to corresponding responses.</li>
     *   <li>The value is a {@link CompletableFuture} that expected to be completed upon receiving response for the
     *   authentication request.
     * </ul>
     * </p>
     */
    private final Map<UUID, CompletableFuture<AuthenticationResponse>> pendingRequests = new ConcurrentHashMap<>();

    @PostMapping(value = "/authorize", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> authorize(@RequestBody AuthorizationRequest request) {

        int identifierLength = request.getDriverIdentifier().getId().length();
        if (identifierLength < 20 || identifierLength > 80) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthorizationResponse(AuthorizationStatus.INVALID));
        }

        String authToken = jwtUtil.generateToken(request.getDriverIdentifier().getId());

        AuthenticationRequest authRequest = AuthenticationRequest.builder()
                                                                .requestId(UUID.randomUUID())
                                                                .authenticationToken(authToken)
                                                                .build();
        CompletableFuture<AuthenticationResponse> future = new CompletableFuture<>();
        pendingRequests.put(authRequest.getRequestId(), future);

        kafkaProducer.sendAuthRequest(authRequest);

        try {
            AuthenticationResponse response = future.get(10, TimeUnit.SECONDS);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new AuthorizationResponse(response.getStatus()));
        } catch (TimeoutException e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @KafkaListener(topics = "authentication-responses")
    public void handleAuthResponse(AuthenticationResponse response) {
        CompletableFuture<AuthenticationResponse> future = pendingRequests.remove(response.getRequestId());
        if (future != null) {
            future.complete(response);
        }
    }
}
