package com.back.domain.review.controller

import com.back.domain.member.service.MemberService
import com.back.domain.review.entity.Review
import com.back.domain.review.service.ReviewService
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ApiV1ReviewControllerGetTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var reviewService: ReviewService

    @Autowired
    private lateinit var memberService: MemberService

    @Test
    @DisplayName("리뷰 다건 조회")
    @Throws(Exception::class)
    fun t1() {
        val bookId = 1L
        val reviews: List<Review> = reviewService.getReviewsByBookId(bookId)

        val resultActions = mvc
            .perform(
                get("/api/v1/reviews/book/$bookId")
            )
            .andDo(print())

        resultActions
            .andExpect(status().isOk())

        for (i in reviews.indices) {
            val review = reviews[i]
            resultActions
                .andExpect(jsonPath("$[$i].id").value(review.id))
                .andExpect(jsonPath("$[$i].rating").value(review.rating))
                .andExpect(jsonPath("$[$i].content").value(review.content))
                .andExpect(
                    jsonPath("$[$i].modifiedDate")
                        .value(Matchers.startsWith(review.modifiedDate.toString().take(20)))
                )
                .andExpect(
                    jsonPath("$[$i].createdDate")
                        .value(Matchers.startsWith(review.createdDate.toString().take(20)))
                )
                .andExpect(jsonPath("$[$i].reviewer").exists())
                .andExpect(jsonPath("$[$i].reviewer.id").value(review.reviewer.id))
                .andExpect(
                    jsonPath("$[$i].reviewer.githubId")
                        .value(review.reviewer.githubId)
                )
                .andExpect(
                    jsonPath("$[$i].reviewer.githubLink")
                        .value(review.reviewer.githubLink)
                )
                .andExpect(jsonPath("$[$i].tags").exists())

            val tags: List<String> = review.tags

            for (j in tags.indices) {
                resultActions
                    .andExpect(jsonPath("$[$i].tags[$j]").value(tags[j]))
            }
        }
    }

    @Test
    @DisplayName("리뷰 다건 조회 - 실패: 존재하지 않는 도서")
    @Throws(Exception::class)
    fun t4() {
        val bookId = 111111L

        val resultActions = mvc
            .perform(
                get("/api/v1/reviews/book/$bookId")
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiReviewControllerV1::class.java))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("404-1"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 도서입니다."))
    }

    @Test
    @DisplayName("특정 회원이 작성한 리뷰 목록 조회")
    @Throws(Exception::class)
    fun t2() {
        val memberId = 3L
        val member = memberService.getById(memberId)
        val ratings: Map<String, Any> = reviewService.getRatingMap(member.id)
        val reviews: List<Review> = reviewService.getByMemberId(member.id)

        val resultActions = mvc
            .perform(
                get("/api/v1/reviews/member/$memberId")
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiReviewControllerV1::class.java))
            .andExpect(handler().methodName("getReviewsByMember"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rating").exists())
            .andExpect(jsonPath("$.results").exists())

        for (rating in ratings.entries) {
            resultActions
                .andExpect(
                    jsonPath("$.rating.[\"${rating.key}\"]").value(rating.value)
                )
        }

        for (i in reviews.indices) {
            val review = reviews[i]
            resultActions
                .andExpect(jsonPath("$.results[$i].id").value(review.id))
                .andExpect(jsonPath("$.results[$i].rating").value(review.rating))
                .andExpect(jsonPath("$.results[$i].content").value(review.content))
                .andExpect(jsonPath("$.results[$i].tags").exists())

            val tags: List<String> = review.tags

            for (j in tags.indices) {
                resultActions
                    .andExpect(
                        jsonPath("$.results[$i].tags[$j]").value(tags[j])
                    )
            }
        }
    }

    @Test
    @DisplayName("특정 회원이 작성한 리뷰 목록 조회 - 실패: 존재하지 않는 회원")
    @Throws(Exception::class)
    fun t5() {
        val memberId = 111111L

        val resultActions = mvc
            .perform(
                get("/api/v1/reviews/member/$memberId")
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiReviewControllerV1::class.java))
            .andExpect(handler().methodName("getReviewsByMember"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("404-1"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 회원입니다."))
    }

    @Test
    @DisplayName("내가 작성한 리뷰 목록 조회")
    @WithUserDetails("user1")
    @Throws(Exception::class)
    fun t3() {
        val resultActions = mvc
            .perform(
                get("/api/v1/reviews/member/mine")
            )
            .andDo(print())

        val member = memberService.getByUsername("user1")
        val ratings: Map<String, Any> = reviewService.getRatingMap(member.id)
        val reviews: List<Review> = reviewService.getByMemberId(member.id)

        resultActions
            .andExpect(handler().handlerType(ApiReviewControllerV1::class.java))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rating").exists())
            .andExpect(jsonPath("$.results").exists())


        for (rating in ratings.entries) {
            resultActions
                .andExpect(
                    jsonPath("$.rating.[\"${rating.key}\"]").value(rating.value)
                )
        }

        for (i in reviews.indices) {
            val review = reviews[i]
            resultActions
                .andExpect(jsonPath("$.results[$i].id").value(review.id))
                .andExpect(jsonPath("$.results[$i].rating").value(review.rating))
                .andExpect(jsonPath("$.results[$i].content").value(review.content))
                .andExpect(jsonPath("$.results[$i].tags").exists())

            val tags: List<String> = review.tags

            for (j in tags.indices) {
                resultActions
                    .andExpect(
                        jsonPath("$.results[$i].tags[$j]").value(tags[j])
                    )
            }
        }
    }

    @Test
    @DisplayName("내가 작성한 리뷰 목록 조회 - 실패: 로그인 하지 않은 사용자")
    @Throws(Exception::class)
    fun t6() {
        val resultActions = mvc
            .perform(
                get("/api/v1/reviews/member/mine")
            )
            .andDo(print())

        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.resultCode").value("401-1"))
            .andExpect(jsonPath("$.message").value("로그인 후 이용해주세요."))
    }
}