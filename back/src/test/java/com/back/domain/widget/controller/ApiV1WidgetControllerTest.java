package com.back.domain.widget.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1WidgetControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("위젯 조회")
    void t1() throws Exception {
        String githubId = "githubuser1";

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/widgets/%s".formatted(githubId)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1WidgetController.class))
                .andExpect(handler().methodName("getWidget"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("image/svg+xml"));
    }
}
