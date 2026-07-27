package com.back.domain.widget.controller;

import com.back.domain.widget.service.WidgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.http.HttpStatus.OK;

@Controller
@RequestMapping("/api/v1/widgets")
@RequiredArgsConstructor
public class ApiV1WidgetController {
    private final WidgetService widgetService;

    @GetMapping("{githubId}")
    public ResponseEntity<String> getWidget(
            @PathVariable
            String githubId
    ) {
        String widgetString = widgetService.createWidget(githubId);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType("image/svg+xml"));

        return new ResponseEntity<>(
                widgetString,
                httpHeaders,
                OK
        );
    }
}
