package com.back.domain.book.service;

import com.back.domain.book.dto.BookDto;
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
    private BookViewsService bookViewsService;

//    @Test
//    @DisplayName("조회수 기반 도서 순위 조회")
//    void t1() {
//
//        List<Integer> viewsCount = List.of(3, 5, 1);
//
//        for (int i = 0; i < viewsCount.size(); i++) {
//            for (int j = 0; j < viewsCount.get(i); j++) {
//                bookViewsService.incrementViewCount((long)(i + 1));
//            }
//        }
//
//        List<BookDto> bookRank = bookViewsService.topViewedInLastHour(0, 0);
//
//        int upperCnt = Integer.MAX_VALUE;
//
//        for (int i = 0; i < bookRank.size(); i++) {
//            long id = bookRank.get(i).id();
//            int nowCnt = bookViewsService.getViewCount(id);
//
//            assertThat(nowCnt).isEqualTo(viewsCount.get((int)(id - 1)));
//            assertThat(upperCnt).isGreaterThanOrEqualTo(nowCnt);
//
//            upperCnt = nowCnt;
//
//        }
//
//    }
}
