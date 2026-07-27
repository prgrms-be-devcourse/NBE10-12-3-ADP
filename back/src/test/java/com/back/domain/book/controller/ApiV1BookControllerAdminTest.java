package com.back.domain.book.controller;

import com.back.domain.book.entity.Book;
import com.back.domain.book.repository.BookRepository;
import com.back.domain.review.repository.ReviewRepository;
import com.back.domain.wish.repository.WishRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1BookControllerAdminTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private WishRepository wishRepository;

    @Test
    @DisplayName("도서 정보 수정 - 관리자")
    @WithUserDetails("admin")
    void t1() throws Exception {

        Book book = bookRepository.findAll().get(0);

        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/books/%d".formatted(book.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "수정된 제목",
                                            "description": "수정된 설명",
                                            "authors": "수정된 작가",
                                            "publisher": "수정된 출판사",
                                            "imgUrl": "https://example.com/img.png"
                                        }
                                        """))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1BookController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.message").value("도서 정보 수정 완료"))
                .andExpect(jsonPath("$.data.title").value("수정된 제목"));

        Book modified = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(modified.getTitle()).isEqualTo("수정된 제목");
        assertThat(modified.getDescription()).isEqualTo("수정된 설명");
        assertThat(modified.getAuthors()).isEqualTo("수정된 작가");
        assertThat(modified.getPublisher()).isEqualTo("수정된 출판사");
        assertThat(modified.getImgUrl()).isEqualTo("https://example.com/img.png");
    }

    @Test
    @DisplayName("도서 정보 수정 - 실패: 관리자가 아님")
    @WithUserDetails("user1")
    void t2() throws Exception {

        Book book = bookRepository.findAll().get(0);

        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/books/%d".formatted(book.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "수정된 제목"
                                        }
                                        """))
                .andDo(print());

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.message").value("권한이 없습니다."));
    }

    @Test
    @DisplayName("도서 삭제 - 관리자 (리뷰/찜 함께 삭제)")
    @WithUserDetails("admin")
    void t3() throws Exception {

        Book book = bookRepository.findAll().get(0);
        long bookId = book.getId();

        assertThat(reviewRepository.findByBook(book)).isNotEmpty();

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/books/%d".formatted(bookId)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1BookController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.message").value("도서 삭제 완료"));

        assertThat(bookRepository.findById(bookId)).isEmpty();
    }

    @Test
    @DisplayName("도서 삭제 - 실패: 관리자가 아님")
    @WithUserDetails("user1")
    void t4() throws Exception {

        Book book = bookRepository.findAll().get(0);

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/books/%d".formatted(book.getId())))
                .andDo(print());

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.message").value("권한이 없습니다."));
    }
}
