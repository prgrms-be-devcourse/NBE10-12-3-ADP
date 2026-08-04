package com.back.domain.review.controller

import com.back.domain.review.controller.request.ReviewCreateRequest
import com.back.domain.review.controller.request.ReviewUpdateRequest
import com.back.domain.review.dto.ReviewDto
import com.back.domain.review.service.ReviewService
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "ApiReviewControllerV1", description = "API 리뷰 컨트롤러 v1")
class ApiReviewControllerV1(
    private val reviewService: ReviewService,
    private val rq: Rq
) {

    @GetMapping("/latest")
    @Operation(summary = "리뷰 최신순 다건 조회")
    fun getReviewsOrderByLatest(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ) = reviewService.getReviewsOrderByLatest(page, size)

    @GetMapping("/book/{bookId}")
    @Operation(summary = "도서별 리뷰 다건 조회")
    fun getReviewsByBookId(
        @PathVariable bookId: Long
    ) = reviewService.getReviewsByBookId(bookId)

    @GetMapping("/member/{memberId}")
    @Operation(summary = "회원별 리뷰 다건 조회")
    fun getReviewsByMemberId(
        @PathVariable memberId: Long
    ) = reviewService.getReviewsByMemberId(memberId)

    @GetMapping("/member/mine")
    @Operation(summary = "내 리뷰 다건 조회")
    @SecurityRequirement(name = "bearerAuth")
    fun getMyReviews()
        = reviewService.getReviewsByMemberId(rq.actor.id)

    @GetMapping("/admin")
    @Operation(summary = "리뷰 다건 조회 (관리자)")
    @SecurityRequirement(name = "bearerAuth")
    fun getReviews(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ) = reviewService.getReviews(page, size)

    @PostMapping("/book/{bookId}")
    @Operation(summary = "리뷰 생성")
    @SecurityRequirement(name = "bearerAuth")
    fun createReview(
        @PathVariable bookId: Long,
        @RequestBody @Valid req: ReviewCreateRequest
    ): RsData<ReviewDto> {
        val rating = requireNotNull(req.rating)

        val review = reviewService.createReview(
            rq.actor, bookId,
            rating, req.content, req.tags
        )

        return RsData("201-1", "리뷰 생성을 성공했습니다.", review)
    }

    @PutMapping("/{id}")
    @Operation(summary = "리뷰 수정")
    @SecurityRequirement(name = "bearerAuth")
    fun updateReview(
        @PathVariable id: Long,
        @RequestBody @Valid req: ReviewUpdateRequest
    ): RsData<ReviewDto> {
        val rating = requireNotNull(req.rating)

        val review = reviewService.updateReview(
            rq.actor, id,
            rating, req.content, req.tags
        )

        return RsData("200-1", "리뷰 수정을 성공했습니다.", review)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "리뷰 삭제")
    @SecurityRequirement(name = "bearerAuth")
    fun deleteReview(
        @PathVariable id: Long
    ): RsData<Unit> {
        reviewService.deleteReview(rq.actor, id)

        return RsData("200-1", "리뷰 삭제를 성공했습니다.")
    }
}
