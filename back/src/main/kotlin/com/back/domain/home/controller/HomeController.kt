package com.back.domain.home.controller

import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpStatus.OK
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeController {
    @GetMapping("/health")
    @ResponseStatus(OK)
    fun healthCheck() {
        // 헬스체크
    }

    @GetMapping("/session")
    // @Operation(summary = "세션 확인")
    fun session(session: HttpSession): Map<String, Any?> =
        session.attributeNames.toList().associateWith(session::getAttribute)
}
