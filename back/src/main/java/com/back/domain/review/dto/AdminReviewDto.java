package com.back.domain.review.dto;

import com.back.domain.member.dto.MemberDto;
import com.back.domain.review.entity.Review;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record AdminReviewDto(
        @NotNull
        Long id,
        @NotNull
        String bookTitle,
        @NotNull
        Float rating,
        @NotNull
        String content,
        @NotNull
        LocalDateTime createdDate,
        @NotNull
        MemberDto reviewer,
        @NotNull
        List<String> tags
) {
    public AdminReviewDto(Review review) {
        this(
                review.getId(),
                review.getBook().getTitle(),
                review.getRating(),
                review.getContent(),
                review.getCreatedDate(),
                new MemberDto(review.getReviewer()),
                review.getTags());
    }
}