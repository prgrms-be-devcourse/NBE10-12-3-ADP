package com.back.domain.book.service

import com.back.domain.book.entity.Book
import com.back.domain.book.repository.BookRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest

@org.springframework.stereotype.Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
class BookRankService(
    private val bookRepository: BookRepository
) {

    fun getBooksOrderByRating(page: Int, size: Int): Page<Book> {
        val pageable = PageRequest.of(page, size)
        return bookRepository.findAllByOrderByAverageRatingDesc(pageable)
    }

    fun getBooksOrderByReviewCnt(page: Int, size: Int): Page<Book> {

        val pageable = PageRequest.of(page, size)

        return bookRepository.findAllByOrderByReviewCountDesc(pageable)
    }
}
