package com.back.global.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
public class SecurityUser extends User implements OAuth2User {
    private final long id;
    private final String name;

    public SecurityUser(
            long id,
            String username,
            String name,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(username, "", authorities); // 우리의 시나리오(REST API)에서는 이 객체의 비밀번호 필드를 활용할 일이 없다.
        this.id = id;
        this.name = name;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }
}