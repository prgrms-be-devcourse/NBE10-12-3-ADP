package com.back.domain.book.service

import com.back.domain.book.entity.Book
import com.back.domain.book.repository.BookRepository
import com.back.domain.book.repository.BookViewCountRedisRepository
import com.back.global.rq.Rq
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BookViewsService(
    private val rq: Rq,
    private val bookRepository: BookRepository,
    private val bookService: BookService,
    private val bookViewCountRedisRepository: BookViewCountRedisRepository
) {
}
