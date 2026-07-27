package com.back.domain.home.controller;

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
class HomeControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("헬스체크")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/health"))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(HomeController.class))
                .andExpect(handler().methodName("healthCheck"))
                .andExpect(status().isOk());
    }
}
