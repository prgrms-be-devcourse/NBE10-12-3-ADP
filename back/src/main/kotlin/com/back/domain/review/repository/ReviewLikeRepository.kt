package com.back.domain.review.repository

import com.back.domain.member.entity.Member
import com.back.domain.review.entity.Review
import com.back.domain.review.entity.ReviewLike
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewLikeRepository : JpaRepository<ReviewLike, Long> {
    fun existsByReviewAndMember(review: Review, member: Member): Boolean
}
