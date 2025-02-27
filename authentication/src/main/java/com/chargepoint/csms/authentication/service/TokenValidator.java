package com.chargepoint.csms.authentication.service;

import com.chargepoint.csms.authentication.config.SecurityConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenValidator {

    private final SecurityConfig securityConfig;

    public String extractIdentifier(final String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(Keys.hmacShaKeyFor(securityConfig.getJwtSecretKey().getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Token could not be parsed due to: ", e);
            return null;
        }
    }
}
