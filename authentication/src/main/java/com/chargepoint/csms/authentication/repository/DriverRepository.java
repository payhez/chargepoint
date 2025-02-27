package com.chargepoint.csms.authentication.repository;

import com.chargepoint.csms.authentication.entity.Driver;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DriverRepository extends ReactiveCrudRepository<Driver, String> {

    Mono<Driver> findByDriverIdentifier(String driverIdentifier);
}