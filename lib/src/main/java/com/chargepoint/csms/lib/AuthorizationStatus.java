package com.chargepoint.csms.lib;

import lombok.Getter;

@Getter
public enum AuthorizationStatus {
    ACCEPTED("Accepted"),
    INVALID("Invalid"),
    UNKNOWN("Unknown"),
    REJECTED("Rejected"),
    INTERNAL_ERROR("Internal Error");

    private final String status;

    AuthorizationStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}