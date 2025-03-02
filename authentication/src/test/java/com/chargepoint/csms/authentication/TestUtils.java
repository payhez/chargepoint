package com.chargepoint.csms.authentication;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class TestUtils {

    @Value("${security.jwt-secret-key}")
    private String jwtSecretKey;

    public String generateToken(String identifier) {
        return Jwts.builder()
                .setSubject(identifier)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000)) // expires after 1 minute
                .signWith(SignatureAlgorithm.HS256, jwtSecretKey.getBytes(StandardCharsets.UTF_8))
                .compact();
    }


}
