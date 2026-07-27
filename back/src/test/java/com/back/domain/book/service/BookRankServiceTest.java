package com.back.domain.book.service;

import com.back.domain.book.dto.BookDto;
import com.back.domain.review.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
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
    private BookRankService bookRankService;
    @Autowired
    private BookService bookService;

    @Test
    @DisplayName("리뷰 수 기반 도서 순위 조회")
    void t1() {

        List<BookDto> bookRank = bookRankService.getBooksByReviewCnt(0, 100);

        for (var book : bookRank) {
            System.out.println("출력: " + book.id());
        }

        int upperCnt = reviewService.findByBookId(bookRank.getFirst().id()).size();

        for (int i = 1; i < bookRank.size(); i++) {
            int nowCnt = reviewService.findByBookId(bookRank.get(i).id()).size();

            assertThat(upperCnt).isGreaterThanOrEqualTo(nowCnt);

            upperCnt = nowCnt;

        }

    }

    @Test
    @DisplayName("평점 기반 도서 순위 조회")
    void t2(){

        List<BookDto> bookRank = bookRankService.getBooksByRating(0, 100);
        double upperRating = bookService.getPureBook(bookRank.getFirst().id()).getAverageRating();

        for (int i = 1; i < bookRank.size(); i++) {
            double nowRating = bookService.getPureBook(bookRank.get(i).id()).getAverageRating();

            assertThat(upperRating).isGreaterThanOrEqualTo(nowRating);

            upperRating = nowRating;

        }

    }
}
