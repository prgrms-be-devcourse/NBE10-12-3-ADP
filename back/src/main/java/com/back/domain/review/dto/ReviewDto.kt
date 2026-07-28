package com.back.domain.review.dto

import com.back.domain.member.dto.MemberDto
import com.back.domain.review.entity.Review
import java.time.LocalDateTime

@JvmRecord
data class ReviewDto(
    val id: Long,
    val bookId: Long,
    val rating: Float,
    val content: String,
    val modifiedDate: LocalDateTime,
    val createdDate: LocalDateTime,
    val reviewer: MemberDto,
    val tags: List<String>
) {
    constructor(review: Review) : this(
        review.id,
        review.book.id,
        review.rating,
        review.content,
        review.modifiedDate,
        review.createdDate,
        MemberDto(review.reviewer),
        review.tags
    )
}
