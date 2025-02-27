package com.chargepoint.csms.transaction.controller.request;

import com.chargepoint.csms.transaction.model.DriverIdentifier;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AuthorizationRequest {
    private UUID stationUuid;
    private DriverIdentifier driverIdentifier;
}
