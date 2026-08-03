package com.back.domain.review.repository

import com.back.domain.book.entity.Book
import com.back.domain.member.entity.Member
import com.back.domain.review.entity.Review
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

interface ReviewRepository : JpaRepository<Review, Long> {

    fun findByBook(book: Book): List<Review>
    fun findByBook(book: Book, pageable: Pageable): Page<Review>

    fun findByBookId(bookId: Long): List<Review>

    fun findByReviewer(member: Member): List<Review>
    fun findByReviewer(member: Member, pageable: Pageable): Page<Review>

    fun countByReviewerAndRating(member: Member, rating: Float): Int
    fun countByReviewer(member: Member): Int
    fun countByReviewerAndContentNot(member: Member, content: String): Int

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.reviewer = :member")
    fun getAverageRatingByMember(member: Member): Double

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.book = :book")
    fun getAverageRatingByBook(@Param("book") book: Book): Double

    fun countByBook(book: Book): Int

    fun findFirstByOrderByIdDesc(): Review?

    fun countByBookAndRating(book: Book?, rating: Float): Int

    fun findFirstByBookAndReviewer(book: Book, reviewer: Member): Review?

    @Query(
        """
            SELECT r.book AS book, count(*) as CNT
            FROM Review r
            WHERE r.modifiedDate >= :cutoffDate
            GROUP BY r.book
            ORDER BY CNT DESC
           """
    )
    fun findBookByOrderByReviewCnt(
        @Param("cutoffDate") cutoffDate: LocalDateTime?,
        pageable: Pageable?
    ): Page<BookInterface>
}
