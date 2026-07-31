package com.back.domain.book.service;

import com.back.domain.book.dto.BookDto;
import com.back.domain.book.entity.Book;
import com.back.domain.review.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class BookViewsServiceTest {

    @Autowired
    private BookService bookService;

    @Test
    @DisplayName("조회수 기반 도서 순위 조회")
    void t1() {

        List<Integer> viewsCount = List.of(3, 5, 1);

        for (int i = 0; i < viewsCount.size(); i++) {
            for (int j = 0; j < viewsCount.get(i); j++) {
                Book book = bookService.getBookById(i + 1);
                bookService.incrementViewCount(book);
            }
        }

        List<Book> bookRank = bookService.getBooksOrderByRank("views", 0, 10);

        int upperCnt = Integer.MAX_VALUE;

        for (Book book : bookRank) {
            int nowCnt = bookService.getBookViewCount(book);

            System.out.println("view: " + nowCnt);

            assertThat(nowCnt).isEqualTo(viewsCount.get((int) (book.getId() - 1)));
            assertThat(upperCnt).isGreaterThanOrEqualTo(nowCnt);

            upperCnt = nowCnt;

        }

    }
}
