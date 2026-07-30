package com.back.domain.book.repository

import com.back.domain.book.entity.Book
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface BookRepository : JpaRepository<Book, Long> {

    fun existsByIsbn(isbn: String): Boolean

    fun findByTitleContaining(searchTerm: String, pageable: Pageable): Page<Book>

    @Query(
        value = """
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
    fun searchByKeyword(@Param("keyword") keyword: String, pageable: Pageable): Page<Book>

    fun findAllByOrderByAverageRatingDesc(pageable: Pageable?): Page<Book>

    fun findAllByOrderByReviewCountDesc(pageable: Pageable?): Page<Book>
}
