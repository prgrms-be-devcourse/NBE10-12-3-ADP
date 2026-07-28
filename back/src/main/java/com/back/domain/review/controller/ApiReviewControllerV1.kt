package com.back.domain.review.controller

import ReviewCreateRequestDto
import com.back.domain.review.dto.ReviewsByMemberDto
import com.back.domain.review.dto.AdminReviewDto
import com.back.domain.review.dto.ReviewDto
import com.back.domain.review.entity.Review
import com.back.domain.review.service.ReviewService
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.function.Function
import kotlin.collections.map

@RestController
@RequestMapping("/api/v1/reviews")
@Transactional(readOnly = true)
@Tag(name = "ApiV1ReviewController", description = "API 리뷰 컨트롤러")
class ApiReviewControllerV1(
    private val reviewService: ReviewService,
    private val rq: Rq
) {

    @GetMapping("/book/{bookId}")
    @Operation(summary = "리뷰 다건 조회")
    fun getReviewsByBook(
        @PathVariable @Valid bookId: @Valid Long
    ): List<ReviewDto> {
        return reviewService
            .getReviewsByBookId(bookId)
            .map { review: Review -> ReviewDto(review) }
            .toList()
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "특정 회원이 작성한 리뷰 다건 조회")
    fun getReviewsByMember(
        @PathVariable @Valid memberId: @Valid Long
    ): ReviewsByMemberDto {

        return ReviewsByMemberDto(
            reviewService.getRatingMap(memberId),
            reviewService
                .getByMemberId(memberId)
                .map {review: Review -> ReviewDto(review)}
                .toList())
    }

    @GetMapping("/member/mine")
    @Operation(summary = "내가 작성한 리뷰 다건 조회")
    @SecurityRequirement(name = "bearerAuth")
    fun getReviewsMine(): ReviewsByMemberDto {
        return getReviewsByMember(rq.actor.id)
    }

    @GetMapping("/admin")
    @Operation(summary = "리뷰 다건 조회 (관리자)")
    @SecurityRequirement(name = "bearerAuth")
    fun getReviews(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Page<AdminReviewDto> {
        return reviewService.getReviews(page, size)
            .map(Function { review: Review -> AdminReviewDto(review) })
    }

    @PostMapping("/book/{bookId}")
    @Transactional
    @Operation(summary = "리뷰 작성")
    @SecurityRequirement(name = "bearerAuth")
    fun createReview(
        @PathVariable bookId: Long,
        @RequestBody @Valid req: @Valid ReviewCreateRequestDto
    ): RsData<ReviewDto> {

        val review: Review = reviewService.createReview(
            bookId, rq.actor.id,
            req.rating, req.content, req.tags
        )

        return RsData<ReviewDto>(
            "201-1", "리뷰 작성 완료", ReviewDto(review)
        )
    }


    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "리뷰 수정")
    @SecurityRequirement(name = "bearerAuth")
    fun updateReview(
        @PathVariable id: Long,
        @RequestBody @Valid req: @Valid ReviewCreateRequestDto
    ): RsData<ReviewDto?> {

        val review = reviewService.updateReview(
            id, rq.actor.id,
            req.rating, req.content, req.tags
        )

        return RsData<ReviewDto?>(
            "200-1", "리뷰 수정 완료", ReviewDto(review)
        )
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "리뷰 삭제")
    @SecurityRequirement(name = "bearerAuth")
    fun deleteReview(
        @PathVariable id: Long
    ): RsData<Void?> {

        reviewService.deleteReview(id, rq.actor.id)

        return RsData<Void?>(
            "200-1", "리뷰 삭제 완료"
        )
    }
}
