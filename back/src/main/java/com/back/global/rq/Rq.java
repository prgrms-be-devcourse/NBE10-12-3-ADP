package com.back.global.rq;

import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.Role;
import com.back.domain.member.service.MemberService;
import com.back.global.security.SecurityUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Rq {
    private final HttpServletRequest req;
    private final HttpServletResponse resp;

    private final MemberService memberService;

    @Value("${custom.security.cookieSecure}")
    private boolean isSecure;

    @Value("${custom.security.cookieDomain}")
    private String cookieDomain;

    public Member getActor() {
        return Optional.ofNullable(
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                )
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof SecurityUser)
                .map(principal -> (SecurityUser) principal)
                .map(securityUser -> new Member(
                        securityUser.getId(),
                        securityUser.getUsername(),
                        securityUser.getName(),
                        hasAdminAuthority(securityUser.getAuthorities()) ? Role.ADMIN : Role.USER
                ))
                .orElse(null);
    }

    private boolean hasAdminAuthority(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    public String getHeader(String name, String defaultValue) {
        return Optional
                .ofNullable(req.getHeader(name))
                .filter(headerValue -> !headerValue.isBlank())
                .orElse(defaultValue);
    }

    public void setHeader(String name, String value) {
        if (value == null) value = "";

        if (value.isBlank()) {
            req.removeAttribute(name);
        } else {
            resp.setHeader(name, value);
        }
    }

    public String getCookieValue(String name, String defaultValue) {
        return Arrays.stream(Optional.ofNullable(req.getCookies()).orElse(new Cookie[0]))
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(defaultValue);
    }

    public void setCookie(String name, String value, int maxAge) {

        if (value == null) value = "";

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setDomain(cookieDomain);
        cookie.setSecure(isSecure);
        cookie.setAttribute("SameSite", isSecure ? "None" : "Strict");

        if (value.isBlank()) cookie.setMaxAge(0);
        else cookie.setMaxAge(maxAge);

        resp.addCookie(cookie);
    }

    public void setCookie(String name, String value) {
        setCookie(name, value, 60 * 60 * 24 * 365);
    }

    public void deleteCookie(String name) {
        setCookie(name, null);
    }

    @SneakyThrows
    public void sendRedirect(String url) {
        resp.sendRedirect(url);
    }

    public Member getActorFromDb() {
        Member actor = getActor();

        if (actor == null) return null;

        return memberService.findById(actor.getId());
    }
}