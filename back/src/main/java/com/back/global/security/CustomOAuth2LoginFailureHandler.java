package com.back.global.security;

import com.back.global.rq.Rq;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizationFailureHandler;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomOAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final Rq rq;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        String redirectUrl = "/";

        // ✅ state 파라미터 확인
        String stateParam = request.getParameter("state");

        if (stateParam != null) {
            // 1️⃣ Base64 URL-safe 디코딩
            String decodedStateParam = new String(
                    Base64.getUrlDecoder().decode(stateParam),
                    StandardCharsets.UTF_8);

            // 2️⃣ '#' 앞은 redirectUrl, 뒤는 originState
            redirectUrl = decodedStateParam.split("#", 2)[0];
        }

        // ✅ 최종 리다이렉트
        rq.sendRedirect(redirectUrl);
    }
}
