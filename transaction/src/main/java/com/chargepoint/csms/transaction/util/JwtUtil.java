package com.chargepoint.csms.transaction.util;

import com.chargepoint.csms.transaction.config.SecurityConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final SecurityConfig securityConfig;

    public String generateToken(String identifier) {
        return Jwts.builder()
                .setSubject(identifier)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000)) // expires after 1 minute
                .signWith(SignatureAlgorithm.HS256, securityConfig.getJwtSecretKey())
                .compact();
    }
}
