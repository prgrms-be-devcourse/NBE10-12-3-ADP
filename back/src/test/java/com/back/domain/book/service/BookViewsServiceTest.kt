package com.back.domain.book.service

import com.back.domain.book.entity.Book
import org.assertj.core.api.AssertionsForClassTypes
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

        val bookRank: List<Book> = bookService.getBooksOrderByRank("views", 0, 10)

        var upperCnt = Int.MAX_VALUE

        for (book in bookRank) {
            val nowCnt = bookService.getBookViewCount(book)

            AssertionsForClassTypes.assertThat(nowCnt).isEqualTo(viewsCount[(book.id - 1).toInt()])
            AssertionsForClassTypes.assertThat(upperCnt).isGreaterThanOrEqualTo(nowCnt)

            upperCnt = nowCnt
        }
    }
}
