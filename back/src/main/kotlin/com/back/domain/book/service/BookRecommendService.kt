package com.back.domain.book.service

import com.back.domain.book.dto.BookDto
import com.back.domain.book.repository.BookRepository
import com.back.domain.member.entity.Member
import com.back.domain.review.entity.Review
import com.back.domain.review.repository.ReviewRepository

import com.back.standard.recommend.byRating.SimilarityRecommendByRating
import com.back.standard.recommend.byRating.SimilarityRecommendByRating.Rating
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BookRecommendService(
    private val reviewRepository: ReviewRepository,
    private val bookRepository: BookRepository
) {

    private fun reviewToRecommendReview(review: Review): Rating {
        return Rating(
            review.reviewer.id,
            review.book.id,
            review.rating
        )
    }

    private fun recommendReviewsByReviewer(reviewer: Member): List<Rating> {
        return reviewRepository.findByReviewer(reviewer, PageRequest.of(0, 5))
            .toList()
            .map { review: Review -> this.reviewToRecommendReview(review) }
    }

    fun getBooksByRecommend(actor: Member?): List<BookDto> {
        if (actor == null) return listOf()

        val recommendSystem = SimilarityRecommendByRating()

        val recentReviews: List<Review> = reviewRepository
            .findByReviewer(actor, PageRequest.of(0, 5))
            .toList()

        recommendSystem.setData(
            recommendReviewsByReviewer(actor)
        )

        val members: MutableSet<Member> = mutableSetOf()

        for (review in recentReviews) {
            val book = bookRepository.findByIdOrNull(review.book.id) ?: continue

            reviewRepository
                .findByBook(book,PageRequest.of(0, 10))
                .forEach { r -> members.add(r.reviewer) }
        }

        for (reviewer in members) {
            recommendSystem.setData(
                recommendReviewsByReviewer(reviewer)
            )
        }

        return recommendSystem.getRecommendList(actor.id, 5, 10)
            .map { bookId -> BookDto(bookRepository.findByIdOrNull(bookId)!!) }
    }
}
