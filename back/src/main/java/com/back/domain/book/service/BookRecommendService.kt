package com.back.domain.book.service

import com.back.domain.book.entity.Book
import com.back.domain.book.repository.BookRepository
import com.back.domain.member.entity.Member
import com.back.domain.review.entity.Review
import com.back.domain.review.service.ReviewService

import com.back.standard.recommend.byRating.SimilarityRecommendByRating
import com.back.standard.recommend.byRating.SimilarityRecommendByRating.Rating
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BookRecommendService(
    private val reviewService: ReviewService,
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
        return reviewService.getByMember(reviewer, 0, 5)
            .toList()
            .map { review: Review -> this.reviewToRecommendReview(review) }
    }

    fun getBooksByRecommend(actor: Member): List<Book> {
        val recommendSystem = SimilarityRecommendByRating()

        val recentReviews: List<Review> = reviewService
            .getByMember(actor, 0, 5)
            .stream()
            .toList()

        recommendSystem.setData(
            recommendReviewsByReviewer(actor)
        )

        val members: MutableSet<Member> = mutableSetOf()

        for (review in recentReviews)
            reviewService.getReviewsByBookId(review.book.id, 0, 10)
                .stream()
                .forEach { r: Review? -> members.add(r.reviewer) }

        for (reviewer in members) {
            recommendSystem.setData(
                recommendReviewsByReviewer(reviewer)
            )
        }

        return recommendSystem.getRecommendList(actor.id, 5, 10)
            .map { bookId -> bookRepository.findById(bookId).get() }
    }
}
