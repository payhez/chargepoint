package com.chargepoint.csms.transaction.controller.response;

import com.chargepoint.csms.transaction.enums.AuthorizationStatus;

public class AuthorizationResponse {
    private AuthorizationStatus authorizationStatus;

    public AuthorizationResponse() {}

    public AuthorizationResponse(AuthorizationStatus authorizationStatus) {
        this.authorizationStatus = authorizationStatus;
    }

    public AuthorizationStatus getAuthorizationStatus() {
        return authorizationStatus;
    }
}
