package com.back.domain.book.repository

import com.back.domain.book.entity.BookOperational
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BookOperationalRepository : JpaRepository<BookOperational, Long> {

    fun findByIsbn(isbn: String) : BookOperational?
    fun findByIsbnIn(isbns: Collection<String>): List<BookOperational>
    fun deleteByIsbn(isbn: String)

}
