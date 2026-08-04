package com.back.domain.book.service

import com.back.domain.book.repository.BookOperationalRepository
import com.back.domain.book.repository.BookRepository
import com.back.domain.review.service.ReviewService
import com.back.global.exception.ServiceException
import org.assertj.core.api.AssertionsForClassTypes
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class BookRankServiceTest(@Autowired private val bookRepository: BookRepository) {
    @Autowired
    private lateinit var reviewService: ReviewService

    @Autowired
    private lateinit var bookService: BookService

    @Autowired
    private lateinit var bookOperationalRepository: BookOperationalRepository

    @Test
    @DisplayName("도서 순위(리뷰수) 다건 조회")
    fun t1() {
        val bookRank = bookService.getBooksOrderByRank("reviewCnt", 0, 100)

        if (bookRank.isEmpty()) return

        var upperCnt = reviewService.getReviewsByBookId(bookRank[0].id).size

        for (i in 1..<bookRank.size) {
            val nowCnt = reviewService.getReviewsByBookId(bookRank[i].id).size

            assertThat(upperCnt).isGreaterThanOrEqualTo(nowCnt)

            upperCnt = nowCnt
        }
    }

    fun getAverageRating(id: Long)
        = bookRepository.findByIdOrNull(id)?.let {
            bookOperationalRepository
                .findByIsbn(it.isbn)?.averageRating
                ?: throw ServiceException("404-1", "오류")
    }

    @Test
    @DisplayName("도서 순위(평점) 다건 조회")
    fun t2() {
        val bookRank = bookService.getBooksOrderByRank("rating", 0, 100)

        if (bookRank.isEmpty()) return

        var upperRating = getAverageRating(bookRank[0].id)

        for (i in 1..<bookRank.size) {
            val nowRating = getAverageRating(bookRank[i].id)

            AssertionsForClassTypes.assertThat(upperRating).isGreaterThanOrEqualTo(nowRating)

            upperRating = nowRating
        }
    }
}
