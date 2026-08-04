package com.back.domain.book.service

import com.back.domain.book.entity.Book
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
class BookViewsServiceTest {
    @Autowired
    private lateinit var bookService: BookService

    @Test
    @DisplayName("도서 인기순(조회수) 다건 조회")
    fun t1() {
        val viewsCount = listOf(3, 5, 1)

        for (i in viewsCount.indices)
            (0..<viewsCount[i]).forEach { _ ->
                val book = bookService.getBookById((i + 1).toLong())
                bookService.incrementViewCount(book)
            }

        val bookRank = bookService.getBooksOrderByRank("views", 0, 10)

        var upperCnt = Int.MAX_VALUE

        println()
        for (book in bookRank) {
            println("aaa${book.id}")
            val b = bookService.getBookById(book.id)
            val nowCnt = bookService.getBookViewCount(b)

            val expectedViewCount =
                if (book.id <= viewsCount.size) viewsCount[(book.id - 1).toInt()]
                else 0

            assertThat(nowCnt).isEqualTo(expectedViewCount)
            assertThat(upperCnt).isGreaterThanOrEqualTo(nowCnt)

            upperCnt = nowCnt
        }
    }
}
