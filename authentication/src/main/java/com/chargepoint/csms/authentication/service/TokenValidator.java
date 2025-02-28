package com.chargepoint.csms.authentication.service;

import com.chargepoint.csms.authentication.config.SecurityConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenValidator {

    private final SecurityConfig securityConfig;

    public String extractIdentifier(final String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(securityConfig.getJwtSecretKey().getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Token could not be parsed due to: ", e);
            return null;
        }
    }
}
