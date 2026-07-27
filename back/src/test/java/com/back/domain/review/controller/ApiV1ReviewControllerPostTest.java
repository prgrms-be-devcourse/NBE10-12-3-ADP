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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class ApiV1ReviewControllerPostTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ReviewService reviewService;

    private ResultActions postReview(long bookId, float rating, String content, List<String> tags)
            throws Exception {

        return mvc
                .perform(
                        post("/api/v1/reviews/book/%d".formatted(bookId))
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
    @DisplayName("리뷰 작성")
    @WithUserDetails("user2")
    void t1() throws Exception {
        long bookId = 1L;

        float rating = 3.5f;
        String content = "책 좋네요 ㅎㅎ";
        List<String> tags = List.of("a", "b");

        ResultActions resultActions = postReview(bookId, rating, content, tags);

        Review review = reviewService.findLatest().get();

        resultActions
                .andExpect(handler().handlerType(ApiV1ReviewController.class))
                .andExpect(handler().methodName("post"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.message").value("리뷰 작성 완료"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(review.getId()))
                .andExpect(jsonPath("$.data.bookId").value(bookId))
                .andExpect(jsonPath("$.data.rating").value(rating))
                .andExpect(jsonPath("$.data.content").value(content))
                .andExpect(jsonPath("$.data.createdDate").value(Matchers.startsWith(review.getCreatedDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.data.tags").exists());

        for (int i = 0; i < tags.size(); i++) {

            resultActions
                    .andExpect(jsonPath("$.data.tags[%d]".formatted(i)).value(tags.get(i)));

        }
    }

    @Test
    @DisplayName("리뷰 작성 - 실패: 이미 작성한 리뷰 존재")
    @WithUserDetails("user1")
    void t2() throws Exception {

        long bookId = 1L;

        float rating = 3.5f;
        String content = "책 좋네요 ㅎㅎ";
        List<String> tags = List.of("a", "b");

        ResultActions resultActions = postReview(bookId, rating, content, tags);

        resultActions
                .andExpect(handler().handlerType(ApiV1ReviewController.class))
                .andExpect(handler().methodName("post"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.resultCode").value("409-1"))
                .andExpect(jsonPath("$.message").value("이미 작성한 리뷰가 있습니다."));
    }

    @Test
    @DisplayName("리뷰 작성 - 실패: bookId의 책을 찾을 수 없음")
    @WithUserDetails("user2")
    void t3() throws Exception {

        long bookId = 1111111111L;

        float rating = 3.5f;
        String content = "책 좋네요 ㅎㅎ";
        List<String> tags = List.of("a", "b");

        ResultActions resultActions = postReview(bookId, rating, content, tags);

        resultActions
                .andExpect(handler().handlerType(ApiV1ReviewController.class))
                .andExpect(handler().methodName("post"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 도서입니다."));
    }

    @Test
    @DisplayName("리뷰 작성 - 실패: 로그인 하지 않은 사용자의 리뷰 작성 시도")
    void t4() throws Exception {

        long bookId = 1L;

        float rating = 3.5f;
        String content = "책 좋네요 ㅎㅎ";
        List<String> tags = List.of("a", "b");

        ResultActions resultActions = postReview(bookId, rating, content, tags);

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.message").value("로그인 후 이용해주세요."));
    }

    private void checkRatingFieldError(float rating) throws Exception {

        long bookId = 1L;
        String content = "책 좋네요 ㅎㅎ";
        List<String> tags = List.of("a", "b");

        ResultActions resultActions = postReview(bookId, rating, content, tags);

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