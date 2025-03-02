package com.chargepoint.csms.authentication;

import com.chargepoint.csms.authentication.entity.Driver;
import com.chargepoint.csms.authentication.repository.DriverRepository;
import com.chargepoint.csms.lib.AuthenticationRequest;
import com.chargepoint.csms.lib.AuthenticationResponse;
import com.chargepoint.csms.lib.AuthorizationStatus;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

@SpringBootTest
public class AuthenticationIT extends TestContainerBase {

    @Autowired
    private KafkaTemplate<UUID, AuthenticationRequest> kafkaTemplate;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private TestUtils testUtils;

    @Test
    public void testHappyPath_Accepted() {

        String driverIdentifier = "32345678901234567890";
        UUID requestId = sendAuthenticationRequestMessageThroughKafka(driverIdentifier);

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                        .until(() -> {
                            ConsumerRecords<String, AuthenticationResponse> responses = consumer.poll(Duration.ofSeconds(10));
                            if (responses.isEmpty()) {
                                return false;
                            }
                            ConsumerRecord<String, AuthenticationResponse> mathcingResponse = null;
                            for (ConsumerRecord<String, AuthenticationResponse> response : responses.records("authentication-responses")) {
                                if (requestId.equals(response.value().getRequestId())) {
                                    mathcingResponse = response;
                                    break;
                                }
                            }
                            assert mathcingResponse != null;
                            Assertions.assertEquals(AuthorizationStatus.ACCEPTED, mathcingResponse.value().getStatus());
                            Driver updatedDriver = driverRepository.findByDriverIdentifier(driverIdentifier).block();
                            assert updatedDriver != null;
                            return updatedDriver.getCredit().equals(new BigDecimal("124.75"));
                        });
    }

    @Test
    public void testRejected() {

        String driverIdentifier = "12345678901234567890";
        UUID requestId = sendAuthenticationRequestMessageThroughKafka(driverIdentifier);

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .until(() -> {
                    ConsumerRecords<String, AuthenticationResponse> responses = consumer.poll(Duration.ofSeconds(10));
                    if (responses.isEmpty()) {
                        return false;
                    }
                    ConsumerRecord<String, AuthenticationResponse> mathcingResponse = null;
                    for (ConsumerRecord<String, AuthenticationResponse> response : responses.records("authentication-responses")) {
                        if (requestId.equals(response.value().getRequestId())) {
                            mathcingResponse = response;
                            break;
                        }
                    }
                    assert mathcingResponse != null;
                    Assertions.assertEquals(AuthorizationStatus.REJECTED, mathcingResponse.value().getStatus());
                    Driver nonUpdatedDriver = driverRepository.findByDriverIdentifier(driverIdentifier).block();
                    assert nonUpdatedDriver != null;
                    return nonUpdatedDriver.getCredit().equals(new BigDecimal("100.50"));
                });
    }

    @Test
    public void testUnknown() {

        String driverIdentifier = "romeRandomIdentifierThatIsUnknown";
        UUID requestId = sendAuthenticationRequestMessageThroughKafka(driverIdentifier);

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .until(() -> {
                    ConsumerRecords<String, AuthenticationResponse> responses = consumer.poll(Duration.ofSeconds(10));
                    if (responses.isEmpty()) {
                        return false;
                    }
                    ConsumerRecord<String, AuthenticationResponse> mathcingResponse = null;
                    for (ConsumerRecord<String, AuthenticationResponse> response : responses.records("authentication-responses")) {
                        if (requestId.equals(response.value().getRequestId())) {
                            mathcingResponse = response;
                            break;
                        }
                    }
                    assert mathcingResponse != null;
                    Assertions.assertEquals(AuthorizationStatus.UNKNOWN, mathcingResponse.value().getStatus());
                    Driver unknownDriver = driverRepository.findByDriverIdentifier(driverIdentifier).block();
                    Assertions.assertNull(unknownDriver);
                    return true;
                });
    }

    @Test
    public void given_ThreeKafkaMessagesAtOnce_ResponseAll() throws InterruptedException {
        String firstDriverId = "32345678901234567890";
        String secondDriverId = "12345678901234567890";
        String thirdDriverId = "romeRandomIdentifierThatIsUnknown";

        UUID firstRequestId = sendAuthenticationRequestMessageThroughKafka(firstDriverId);
        UUID secondRequestId = sendAuthenticationRequestMessageThroughKafka(secondDriverId);
        UUID thirdRequestId = sendAuthenticationRequestMessageThroughKafka(thirdDriverId);

        AtomicReference<ConsumerRecord<String, AuthenticationResponse>> mathcingResponse1 = new AtomicReference<>();
        AtomicReference<ConsumerRecord<String, AuthenticationResponse>> mathcingResponse2 = new AtomicReference<>();
        AtomicReference<ConsumerRecord<String, AuthenticationResponse>> mathcingResponse3 = new AtomicReference<>();

        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .until(() -> {
                    ConsumerRecords<String, AuthenticationResponse> responses = consumer.poll(Duration.ofSeconds(10));
                    if (responses.isEmpty()) {
                        return false;
                    }

                    for (ConsumerRecord<String, AuthenticationResponse> response : responses.records("authentication-responses")) {
                        if (firstRequestId.equals(response.value().getRequestId())) {
                            mathcingResponse1.set(response);
                        } else if (secondRequestId.equals(response.value().getRequestId())) {
                            mathcingResponse2.set(response);
                        } else if (thirdRequestId.equals(response.value().getRequestId())) {
                            mathcingResponse3.set(response);
                        }
                    }

                    if (mathcingResponse1.get() == null || mathcingResponse2.get() == null || mathcingResponse3.get() == null) {
                        return false;
                    }

                    Assertions.assertEquals(AuthorizationStatus.ACCEPTED, mathcingResponse1.get().value().getStatus());
                    Assertions.assertEquals(AuthorizationStatus.REJECTED, mathcingResponse2.get().value().getStatus());
                    Assertions.assertEquals(AuthorizationStatus.UNKNOWN, mathcingResponse3.get().value().getStatus());

                    Driver updatedDriver = driverRepository.findByDriverIdentifier(firstDriverId).block();
                    Assertions.assertNotNull(updatedDriver);
                    Assertions.assertEquals(new BigDecimal("124.75"), updatedDriver.getCredit());

                    Driver nonUpdatedDriver = driverRepository.findByDriverIdentifier(secondDriverId).block();
                    Assertions.assertNotNull(nonUpdatedDriver);
                    Assertions.assertEquals(new BigDecimal("100.50"), nonUpdatedDriver.getCredit());

                    Driver unknownDriver = driverRepository.findByDriverIdentifier(thirdDriverId).block();
                    Assertions.assertNull(unknownDriver);
                    return true;
                });
    }

    private UUID sendAuthenticationRequestMessageThroughKafka(final String driverIdentifier) {
        UUID requestId = UUID.randomUUID();

        AuthenticationRequest request =
                AuthenticationRequest.builder()
                        .requestId(requestId)
                        .authenticationToken(testUtils.generateToken(driverIdentifier))
                        .build();
        try {
            // Mock message comes from Transaction service
            kafkaTemplate.send("authentication-requests", request).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        return requestId;
    }
}
