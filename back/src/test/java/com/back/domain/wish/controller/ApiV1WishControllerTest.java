package com.back.domain.wish.controller;

import com.back.domain.book.entity.Book;
import com.back.domain.book.service.BookService;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.wish.controller.ApiV1WishController;
import com.back.domain.wish.entity.Wish;
import com.back.domain.wish.service.WishService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class ApiV1WishControllerTest {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mvc;

    @Autowired
    private WishService wishService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private BookService bookService;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("찜 목록 조회")
    @WithUserDetails("user1")
    void t1() throws Exception {

        Member actor = memberService.findByUsername("user1");

        List<Wish> wishes = wishService.findByMember(actor);
        int wishesSize = wishes.size();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/wishes/mine")
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1WishController.class))
                .andExpect(handler().methodName("getWishes"))
                .andExpect(status().isOk());

        for (int i = 0; i < wishesSize; i++) {
            Book book = wishes.get(i).getBook();
            List<String> tags = bookService.getBookTags(book);
            resultActions
                    .andExpect(jsonPath("$.[%d].id".formatted(i)).value(book.getId()))
                    .andExpect(jsonPath("$.[%d].title".formatted(i)).value(book.getTitle()))
                    .andExpect(jsonPath("$.[%d].imgUrl".formatted(i)).value(book.getImgUrl()))
                    .andExpect(jsonPath("$.[%d].tags".formatted(i)).isArray())
                    .andExpect(jsonPath("$.[%d].averageRating".formatted(i)).value(book.getAverageRating()));

            for (int j = 0; j < tags.size(); j++) {

                resultActions
                        .andExpect(jsonPath("$[%d].tags[%d]".formatted(i, j)).value(tags.get(j)));

            }
        }
    }

    @Test
    @DisplayName("찜 추가")
    @WithUserDetails("user1")
    void t2() throws Exception {

        Long bookId = 2L;

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/wishes/book/%d".formatted(bookId))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1WishController.class))
                .andExpect(handler().methodName("addWish"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.message").value("찜 추가 성공"));
    }


    @Test
    @DisplayName("찜 삭제")
    @WithUserDetails("user1")
    void t3() throws Exception {

        Long bookId = 1L;

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/wishes/book/%d".formatted(bookId)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1WishController.class))
                .andExpect(handler().methodName("deleteWish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.message").value("찜 삭제 성공"));
    }
}