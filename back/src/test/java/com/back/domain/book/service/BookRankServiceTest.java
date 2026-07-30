package com.back.domain.book.service;

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
@AutoConfigureMockMvc
@Transactional
public class BookRankServiceTest {

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private BookService bookService;

    @Test
    @DisplayName("리뷰 수 기반 도서 순위 조회")
    void t1() {

        List<Book> bookRank = bookService.getBooksOrderByRank("reviewCnt", 0, 100);

        for (var book : bookRank) {
            System.out.println("출력: " + book.getId());
        }

        int upperCnt = reviewService.getReviewsByBookId(bookRank.getFirst().getId()).size();

        for (int i = 1; i < bookRank.size(); i++) {
            int nowCnt = reviewService.getReviewsByBookId(bookRank.get(i).getId()).size();

            assertThat(upperCnt).isGreaterThanOrEqualTo(nowCnt);

            upperCnt = nowCnt;

        }

    }

    @Test
    @DisplayName("평점 기반 도서 순위 조회")
    void t2(){

        List<Book> bookRank = bookService.getBooksOrderByRank("rating", 0, 100);

        double upperRating = bookService.getBook(bookRank.getFirst().getId()).getAverageRating();

        for (int i = 1; i < bookRank.size(); i++) {
            double nowRating = bookService.getBook(bookRank.get(i).getId()).getAverageRating();

            assertThat(upperRating).isGreaterThanOrEqualTo(nowRating);

            upperRating = nowRating;

        }

    }
}
