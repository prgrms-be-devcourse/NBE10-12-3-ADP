package com.back.domain.book.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1BookControllerRecommendTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("추천 도서 조회")
    @WithUserDetails("user2")
    void t1() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/books/recommend"))
                .andDo(print());

        resultActions
                .andExpect(jsonPath("$[0].id").value(1));

    }

    @Test
    @DisplayName("추천 도서 조회")
    @WithUserDetails("user3")
    void t2() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/books/recommend"))
                .andDo(print());

        resultActions
                .andExpect(jsonPath("$[0].id").value(4))
                .andExpect(jsonPath("$[1].id").value(1));

    }
}