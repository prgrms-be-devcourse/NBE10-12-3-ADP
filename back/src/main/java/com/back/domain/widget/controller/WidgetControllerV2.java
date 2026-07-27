package com.back.domain.widget.controller;

import com.back.domain.widget.service.WidgetByWidgetServerService;
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
@RequestMapping("/api/v2/widgets")
@RequiredArgsConstructor
public class WidgetControllerV2 {
    private final WidgetByWidgetServerService widgetByWidgetServerService;

    @GetMapping("{githubId}")
    public ResponseEntity<String> getWidget(
            @PathVariable
            String githubId
    ) {
        String widgetString = widgetByWidgetServerService.createWidget(githubId);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType("image/svg+xml"));

        return new ResponseEntity<>(
                widgetString,
                httpHeaders,
                OK
        );
    }
}
