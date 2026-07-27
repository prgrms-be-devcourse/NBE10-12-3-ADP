package com.back.domain.book.repository;

import com.back.domain.book.dto.BookDto;
import com.back.domain.book.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByIsbn(String isbn);

    List<Book> findByTitleContaining(String searchTerm);

    @Query(value = """
            SELECT * FROM Book 
            WHERE MATCH(title, authors, publisher) AGAINST(:keyword IN BOOLEAN MODE)
            LIMIT :#{pageable.pageSize}
            OFFSET :#{pageable.offset}
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM Book
            WHERE MATCH(title, authors, publisher) AGAINST(:keyword IN BOOLEAN MODE)
            """,
            nativeQuery = true
    )
    Page<Book> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Page<Book> findAllByOrderByAverageRatingDesc(Pageable pageable);

    Page<Book> findAllByOrderByReviewCountDesc(Pageable pageable);
}
