package com.chargepoint.csms.transaction.controller.response;

import com.chargepoint.csms.transaction.enums.AuthorizationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthorizationResponse {
    private AuthorizationStatus authorizationStatus;
}
