package com.chargepoint.csms.lib;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthRequest {
    private String requestId;
    private String authToken;
}