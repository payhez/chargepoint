package com.chargepoint.csms.lib;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class AuthenticationRequest {
    private UUID requestId;
    private String authenticationToken;
}