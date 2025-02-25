package com.chargepoint.csms.transaction.enums;

import lombok.Getter;

@Getter
public enum AuthorizationStatus {
    ACCEPTED("Accepted"),
    INVALID("Invalid"),
    UNKNOWN("Unknown"),
    REJECTED("Rejected");

    private final String status;

    AuthorizationStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}