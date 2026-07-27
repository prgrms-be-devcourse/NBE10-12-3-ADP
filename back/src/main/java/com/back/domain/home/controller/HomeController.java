package com.back.domain.home.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.OK;

@RestController
public class HomeController {
    @GetMapping("/health")
    @ResponseStatus(OK)
    public void healthCheck() { /* 헬스체크 */ }

    @GetMapping("/session")
    // @Operation(summary = "세션 확인")
    public Map<String, Object> session(HttpSession session) {
        return Collections.list(session.getAttributeNames()).stream()
                .collect(Collectors.toMap(
                        name -> name,
                        session::getAttribute
                ));
    }
}
