package com.back.domain.widget.controller

import com.back.domain.widget.service.WidgetService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus.OK
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/api/v1/widgets")
class ApiV1WidgetController(
    private val widgetService: WidgetService
) {
    @GetMapping("{githubId}")
    fun getWidget(
        @PathVariable githubId: String
    ): ResponseEntity<String> {
        val widgetString = widgetService.createWidget(githubId)
        val httpHeaders = HttpHeaders().apply {
            contentType = MediaType.parseMediaType("image/svg+xml")
        }

        return ResponseEntity(widgetString, httpHeaders, OK)
    }
}
