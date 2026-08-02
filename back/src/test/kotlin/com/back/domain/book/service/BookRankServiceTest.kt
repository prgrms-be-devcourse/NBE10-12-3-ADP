package com.back.domain.book.service

import com.back.domain.book.entity.Book
import com.back.domain.review.service.ReviewService
import org.assertj.core.api.AssertionsForClassTypes
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class BookRankServiceTest {
    @Autowired
    private lateinit var reviewService: ReviewService

    @Autowired
    private lateinit var bookService: BookService

    @Test
    @DisplayName("도서 인기순(리뷰수) 다건 조회")
    fun t1() {
        val bookRank: List<Book> = bookService.getBooksOrderByRank("reviewCnt", 0, 100)

        if (bookRank.isEmpty()) return

        var upperCnt = reviewService.getReviewsByBookId(bookRank[0].id).size

        for (i in 1..<bookRank.size) {
            val nowCnt = reviewService.getReviewsByBookId(bookRank[i].id).size

            assertThat(upperCnt).isGreaterThanOrEqualTo(nowCnt)

            upperCnt = nowCnt
        }
    }

    @Test
    @DisplayName("도서 인기순(평점) 다건 조회")
    fun t2() {
        val bookRank: List<Book> = bookService.getBooksOrderByRank("rating", 0, 100)

        if (bookRank.isEmpty()) return

        var upperRating = bookService.getBook(bookRank[0].id).averageRating

        for (i in 1..<bookRank.size) {
            val nowRating = bookService.getBook(bookRank[i].id).averageRating

            AssertionsForClassTypes.assertThat(upperRating).isGreaterThanOrEqualTo(nowRating)

            upperRating = nowRating
        }
    }
}
