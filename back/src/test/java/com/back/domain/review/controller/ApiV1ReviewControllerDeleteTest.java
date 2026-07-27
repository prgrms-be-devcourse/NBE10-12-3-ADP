package com.back.domain.review.controller;

import com.back.domain.review.entity.Review;
import com.back.domain.review.service.ReviewService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class ApiV1ReviewControllerDeleteTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ReviewService reviewService;

    private ResultActions deleteReview(long id)
            throws Exception {

        return mvc
                .perform(
                        delete("/api/v1/reviews/%d".formatted(id)))
                .andDo(print());
    }

    @Test
    @DisplayName("리뷰 삭제")
    @WithUserDetails("user1")
    void t1() throws Exception {

        long reviewId = 1L;

        ResultActions resultActions = deleteReview(reviewId);

        resultActions
                .andExpect(handler().handlerType(ApiV1ReviewController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.message").value("리뷰 삭제 완료"));

    }

    @Test
    @DisplayName("리뷰 삭제 - 실패: 작성하지 않은 리뷰 삭제 시도")
    @WithUserDetails("user2")
    void t2() throws Exception {

        long reviewId = 1L;

        ResultActions resultActions = deleteReview(reviewId);

        resultActions
                .andExpect(handler().handlerType(ApiV1ReviewController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.message").value("삭제 권한이 없습니다."));

    }

    @Test
    @DisplayName("리뷰 삭제 - 관리자는 다른 사람의 리뷰도 삭제 가능")
    @WithUserDetails("admin")
    void t2_1() throws Exception {

        long reviewId = 1L;

        ResultActions resultActions = deleteReview(reviewId);

        resultActions
                .andExpect(handler().handlerType(ApiV1ReviewController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.message").value("리뷰 삭제 완료"));

    }

    @Test
    @DisplayName("리뷰 삭제 - 실패: id의 리뷰가 존재하지 않음")
    @WithUserDetails("user1")
    void t3() throws Exception {

        long reviewId = 1111111L;

        ResultActions resultActions = deleteReview(reviewId);

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 리뷰입니다."));

    }

    @Test
    @DisplayName("리뷰 삭제 - 실패: 로그인 하지 않은 사용자의 리뷰 삭제 시도")
    void t4() throws Exception {

        long reviewId = 1L;

        ResultActions resultActions = deleteReview(reviewId);

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.message").value("로그인 후 이용해주세요."));

    }

}