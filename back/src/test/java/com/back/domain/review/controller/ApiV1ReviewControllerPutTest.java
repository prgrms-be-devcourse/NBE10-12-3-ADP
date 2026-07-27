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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class ApiV1ReviewControllerPutTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ReviewService reviewService;

    private ResultActions putReview(long id, float rating, String content, List<String> tags)
            throws Exception {

        return mvc
                .perform(
                        put("/api/v1/reviews/%d".formatted(id))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "rating": %.1f,
                                            "content": "%s",
                                            "tags": ["%s"]
                                        }
                                        """.formatted(rating, content, String.join("\", \"", tags)))
                )
                .andDo(print());
    }

    @Test
    @DisplayName("리뷰 수정")
    @WithUserDetails("user1")
    void t1() throws Exception {

        long id = 1L;

        float rating = 5;
        String content = "다시 읽어보니 더 좋네요.";
        List<String> tags = List.of("a");

        ResultActions resultActions = putReview(id, rating, content, tags);

        Review review = reviewService.findById(id);

        resultActions
                .andExpect(handler().handlerType(ApiV1ReviewController.class))
                .andExpect(handler().methodName("edit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.message").value("리뷰 수정 완료"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.rating").value(rating))
                .andExpect(jsonPath("$.data.content").value(content))
                .andExpect(jsonPath("$.data.modifiedDate").value(Matchers.startsWith(review.getModifiedDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.data.tags").exists());

        for (int i = 0; i <  tags.size(); i++) {
            resultActions
                    .andExpect(jsonPath("$.data.tags[%d]".formatted(i)).value(tags.get(i)));

        }
    }

    @Test
    @DisplayName("리뷰 작성 - 실패: 다른 사람이 작성한 리뷰 수정 시도")
    @WithUserDetails("user2")
    void t2() throws Exception {

        long id = 1L;

        float rating = 3.5f;
        String content = "책 좋네요 ㅎㅎ";
        List<String> tags = List.of("a", "b");

        ResultActions resultActions = putReview(id, rating, content, tags);

        resultActions
                .andExpect(handler().handlerType(ApiV1ReviewController.class))
                .andExpect(handler().methodName("edit"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.message").value("수정 권한이 없습니다."));
    }

    @Test
    @DisplayName("리뷰 작성 - 실패: id의 리뷰를 찾을 수 없음")
    @WithUserDetails("user2")
    void t3() throws Exception {

        long id = 1111111111L;

        float rating = 3.5f;
        String content = "책 좋네요 ㅎㅎ";
        List<String> tags = List.of("a", "b");

        ResultActions resultActions = putReview(id, rating, content, tags);

        resultActions
                .andExpect(handler().handlerType(ApiV1ReviewController.class))
                .andExpect(handler().methodName("edit"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 리뷰입니다."));
    }

    @Test
    @DisplayName("리뷰 작성 - 실패: 로그인 하지 않은 사용자의 리뷰 수정 시도")
    void t4() throws Exception {

        long id = 1L;

        float rating = 3.5f;
        String content = "책 좋네요 ㅎㅎ";
        List<String> tags = List.of("a", "b");

        ResultActions resultActions = putReview(id, rating, content, tags);

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.message").value("로그인 후 이용해주세요."));
    }

    private void checkRatingFieldError(float rating) throws Exception {

        long id = 1L;
        String content = "책 좋네요 ㅎㅎ";
        List<String> tags = List.of("a", "b");

        ResultActions resultActions = putReview(id, rating, content, tags);

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.message").value(
                        "rating-ValidRating-rating은 0~5, 0.5 단위여야 합니다."));

    }

    @Test
    @DisplayName("리뷰 작성 - 실패: 평점이 0.5 단위의 숫자가 아님")
    @WithUserDetails("user2")
    void t5() throws Exception {
        checkRatingFieldError(3.2f);
    }

    @Test
    @DisplayName("리뷰 작성 - 실패: 평점이 5 초과")
    @WithUserDetails("user2")
    void t6() throws Exception {
        checkRatingFieldError(6f);
    }

    @Test
    @DisplayName("리뷰 작성 - 실패: 평점이 0 미만")
    @WithUserDetails("user2")
    void t7() throws Exception {
        checkRatingFieldError(-1f);
    }

}