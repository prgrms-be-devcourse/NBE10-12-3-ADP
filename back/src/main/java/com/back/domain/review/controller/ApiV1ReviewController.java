package com.back.domain.review.controller;

import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.review.dto.AdminReviewDto;
import com.back.domain.review.dto.ReviewDto;
import com.back.domain.review.entity.Review;
import com.back.domain.review.service.ReviewService;
import com.back.domain.review.validation.ValidRating;
import com.back.domain.review.validation.ValidTags;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Tag(name = "ApiV1ReviewController", description = "API 리뷰 컨트롤러")
public class ApiV1ReviewController {
    private final ReviewService reviewService;

    private final MemberService memberService;

    private final Rq rq;

    @GetMapping("/book/{bookId}")
    @Operation(summary = "리뷰 다건 조회")
    public List<ReviewDto> getReviewsByBook(
            @PathVariable @Valid long bookId
    ) {
        return reviewService
                .findByBookId(bookId)
                .stream()
                .map(ReviewDto::new)
                .toList();
    }

    public record ReviewsByMemberDto(
            @NotNull
            Map<String, Object> rating,
            @NotNull
            List<ReviewDto> results
    ) {

    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "특정 회원이 작성한 리뷰 다건 조회")
    public ReviewsByMemberDto getReviewsByMember(
            @PathVariable @Valid long memberId
    ) {
        Member member = memberService.findById(memberId);

        return new ReviewsByMemberDto(
                reviewService.getRatingMap(member),
                reviewService
                        .findByMember(member)
                        .stream()
                        .map(ReviewDto::new)
                        .toList());
    }

    @GetMapping("/member/mine")
    @Operation(summary = "내가 작성한 리뷰 다건 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ReviewsByMemberDto mine() {
        return getReviewsByMember(rq.getActor().getId());
    }

    @GetMapping("/admin")
    @Operation(summary = "리뷰 다건 조회 (관리자)")
    @SecurityRequirement(name = "bearerAuth")
    public Page<AdminReviewDto> getReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return reviewService.getAll(page, size).map(AdminReviewDto::new);
    }

    public record PostReviewsReqBody(
            @NotNull
            @ValidRating
            Float rating,
            @NotNull
            @Size(max = 500)
            String content,
            @NotNull
            @ValidTags
            List<String> tags
    ) {

    }

    @PostMapping("/book/{bookId}")
    @Transactional
    @Operation(summary = "리뷰 작성")
    @SecurityRequirement(name = "bearerAuth")
    public RsData<ReviewDto> post(
            @PathVariable long bookId,
            @RequestBody @Valid PostReviewsReqBody req
    ) {
        Member reviewer = memberService.findById(rq.getActor().getId());

        Review review = reviewService.addReview(
                bookId, reviewer,
                req.rating(), req.content(), req.tags()
        );

        return new RsData<>(
                "201-1", "리뷰 작성 완료", new ReviewDto(review));
    }


    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "리뷰 수정")
    @SecurityRequirement(name = "bearerAuth")
    public RsData<ReviewDto> edit(
            @PathVariable long id,
            @RequestBody @Valid PostReviewsReqBody req
    ) {
        Review review = reviewService.findById(id);
        Member reviewer = memberService.findById(rq.getActor().getId());

        reviewService.editReview(review, reviewer,
                req.rating(), req.content(), req.tags());

        return new RsData<>(
                "200-1", "리뷰 수정 완료", new ReviewDto(review));

    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "리뷰 삭제")
    @SecurityRequirement(name = "bearerAuth")
    public RsData<Void> delete(
            @PathVariable long id
    ) {
        Review review = reviewService.findById(id);
        Member reviewer = memberService.findById(rq.getActor().getId());

        reviewService.deleteReview(review, reviewer);

        return new RsData<>(
                "200-1", "리뷰 삭제 완료");

    }

}
