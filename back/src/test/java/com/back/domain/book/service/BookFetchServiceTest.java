package com.back.domain.book.service;

import com.back.domain.book.entity.Book;
import com.back.domain.book.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class BookFetchServiceTest {

    @Autowired
    private BookFetchService bookFetchService;

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("국립중앙도서관 API에서 실제로 도서 데이터를 가져와 저장한다")
    void t1() {
        bookFetchService.fetch();

        List<Book> books = bookRepository.findAll();
        System.out.println("저장된 도서 수: " + books.size());
        books.forEach(b -> System.out.println(b.getTitle() + " / " + b.getIsbn() + " / " + b.getAuthors()));

        assertThat(books).isNotEmpty();
    }
}