package com.back.domain.review.dto;

import com.back.domain.member.dto.MemberDto;
import com.back.domain.review.entity.Review;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewDto(
        @NotNull
        Long id,
        @NotNull
        Long bookId,
        @NotNull
        Float rating,
        @NotNull
        String content,
        @NotNull
        LocalDateTime modifiedDate,
        @NotNull
        LocalDateTime createdDate,
        @NotNull
        MemberDto reviewer,
        @NotNull
        List<String> tags
) {
    public ReviewDto(Review review) {
        this(
                review.getId(),
                review.getBook().getId(),
                review.getRating(),
                review.getContent(),
                review.getModifiedDate(),
                review.getCreatedDate(),
                new MemberDto(review.getReviewer()),
                review.getTags());
    }
}
