package com.back.domain.review.service

import com.back.domain.book.entity.Book
import com.back.domain.book.repository.BookRepository
import com.back.domain.member.entity.Member
import com.back.domain.member.repository.MemberRepository
import com.back.domain.review.entity.Review
import com.back.domain.review.repository.ReviewRepository
import com.back.domain.tag.entity.Tag
import com.back.domain.tag.repository.TagRepository
import com.back.global.exception.ServiceException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.function.Supplier
import kotlin.jvm.optionals.getOrElse
import kotlin.math.roundToInt

@Service
@Transactional(readOnly = true)
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val bookRepository: BookRepository,
    private val memberRepository: MemberRepository,
    private val tagRepository: TagRepository
) {

    private fun getBookById(bookId: Long) : Book {

        val book = bookRepository.findById(bookId).getOrElse {
            throw ServiceException("404-1", "존재하지 않는 도서입니다.")
        }
        return book
    }

    private fun getMemberById(memberId: Long) : Member {

        val member = memberRepository.findById(memberId).getOrElse {
            throw ServiceException("404-1", "존재하지 않는 회원입니다.")
        }

        return member
    }

    private fun getOrCreateTag(tagName: String) : Tag {
        val tag = tagRepository.findByName(tagName)
        if (tag.isPresent) return tag.get()

        return tagRepository.save(Tag(tagName))
    }

    private fun refreshBookRating(book: Book) {
        val averageRating = reviewRepository.getAverageRatingByBook(book)
        val reviewCount = reviewRepository.countByBook(book)
        book.refreshRating(averageRating, reviewCount)
    }


    fun getReviewsByBookId(bookId: Long): List<Review> {

        return reviewRepository.findByBook(getBookById(bookId))
    }

    fun getReviewsByBookId(bookId: Long, page: Int, size: Int): Page<Review> {

        val pageable: Pageable = PageRequest.of(page, size)

        return reviewRepository.findByBook(getBookById(bookId), pageable)
    }

    fun getByMemberId(memberId: Long): List<Review> {
        val member = getMemberById(memberId)
        return reviewRepository.findByReviewer(member)
    }

    fun getReviews(page: Int, size: Int): Page<Review> {
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        return reviewRepository.findAll(pageable)
    }

    fun getByMember(memberId: Long, page: Int, size: Int): Page<Review> {
        val pageable: Pageable = PageRequest.of(page, size)
        val member = getMemberById(memberId)
        return reviewRepository.findByReviewer(member, pageable)
    }

    fun getReviewCountByMember(memberId: Long): Long {
        val member = getMemberById(memberId)
        return reviewRepository.countByReviewer(member).toLong()
    }

    fun getReviewWithContentCountByMember(memberId: Long): Long {
        val member = getMemberById(memberId)
        return reviewRepository.countByReviewerAndContentNot(member, "").toLong()
    }

    fun getRatingMap(memberId: Long): MutableMap<String, Any> {
        val ratings = mutableMapOf<String, Any>()
        val member = getMemberById(memberId)

        ratings["average"] = (reviewRepository.getAverageRatingByMember(member) * 10.0f).roundToInt() / 10.0f

        for (i in 0..9) {
            val targetRating = (i + 1) * 0.5f
            ratings["%.1f".format(targetRating)] = reviewRepository.countByReviewerAndRating(member, targetRating)
        }

        return ratings
    }

    fun getLatest(): Review? {
        return reviewRepository.findFirstByOrderByIdDesc()
    }

    fun getById(id: Long): Review {
        return reviewRepository.findById(id).orElseThrow(
            Supplier { NoSuchElementException("존재하지 않는 리뷰입니다.") }
        )
    }

    @Transactional
    @Throws(ServiceException::class)
    fun createReview(bookId: Long, actorId: Long,
                     rating: Float, comment: String, tags: List<String>): Review {
        val book = getBookById(bookId)
        val actor = getMemberById(actorId)

        if (reviewRepository.findFirstByBookAndReviewer(book, actor) != null)
            throw ServiceException("409-1", "이미 작성한 리뷰가 있습니다.")

        val review: Review = reviewRepository.save<Review>(
            Review(
                book, actor, rating, comment,
                tags.stream().map { name: String -> getOrCreateTag(name) }.toList()
            )
        )

        refreshBookRating(book)

        return review
    }

    @Transactional
    fun updateReview(reviewId: Long, reviewerId: Long,
                     rating: Float, content: String, tags: List<String>) : Review {

        val review = getById(reviewId)
        val reviewer = getMemberById(reviewerId)

        if (review.reviewer != reviewer) {
            throw ServiceException("403-1", "수정 권한이 없습니다.")
        }

        review.modify(
            rating, content,
            tags.stream().map { name: String -> getOrCreateTag(name) }.toList()
        )

        refreshBookRating(review.book)

        return review
    }

    @Transactional
    fun deleteReview(reviewId: Long, reviewerId: Long) {
        val review = getById(reviewId)
        val reviewer = getMemberById(reviewerId)

        if (review.reviewer != reviewer && !reviewer.isAdmin) {
            throw ServiceException("403-1", "삭제 권한이 없습니다.")
        }

        val book: Book = review.book
        reviewRepository.delete(review)

        refreshBookRating(book)
    }
}
