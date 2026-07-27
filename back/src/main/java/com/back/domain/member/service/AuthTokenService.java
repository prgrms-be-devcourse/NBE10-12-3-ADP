package com.back.domain.member.service;

import com.back.domain.member.entity.Member;
import com.back.standard.util.Ut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthTokenService {
    @Value("${custom.accessToken.expirationSeconds}")
    private int expireSeconds;

    @Value("${custom.jwt.secretKey}")
    private String secret;

    String genAccessToken(Member member) {
        long id = member.getId();
        String username = member.getUsername();
        String name = member.getName();
        String role = member.getRole().name();

        return Ut.jwt.toString(
                secret,
                expireSeconds,
                Map.of("id", id, "username", username, "name", name, "role", role)
        );

    }

    Map<String, Object> payload(String accessToken) {
        Map<String, Object> parsedPayload = Ut.jwt.payload(secret, accessToken);

        if (parsedPayload == null) return null;

        int id = (int) parsedPayload.get("id");
        String username = (String) parsedPayload.get("username");
        String name = (String) parsedPayload.get("name");
        String role = (String) parsedPayload.get("role");

        return Map.of("id", id, "username", username, "name", name, "role", role);
    }
}
