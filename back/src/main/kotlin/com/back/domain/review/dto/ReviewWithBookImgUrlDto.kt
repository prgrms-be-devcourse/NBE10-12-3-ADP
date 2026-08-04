package com.back.domain.review.dto

import com.back.domain.member.dto.MemberDto
import com.back.domain.review.entity.Review
import java.time.LocalDateTime

class ReviewWithBookImgUrlDto(
    id: Long,
    bookId: Long,
    bookTitle: String,
    rating: Float,
    content: String,
    modifiedDate: LocalDateTime,
    createdDate: LocalDateTime,
    reviewer: MemberDto,
    tags: List<String>,
    val bookImgUrl: String?
) : ReviewDto(id, bookId, bookTitle, rating, content, modifiedDate, createdDate, reviewer, tags) {
    constructor(review: Review) : this(
        review.id,
        review.book.id,
        review.book.title,
        review.rating,
        review.content,
        review.modifiedDate,
        review.createdDate,
        MemberDto(review.reviewer),
        review.tags,
        review.book.imgUrl)
}