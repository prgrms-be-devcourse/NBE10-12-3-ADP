package com.back.domain.book.service

import com.back.domain.book.entity.Book
import com.back.domain.book.entity.BookOperational
import com.back.domain.book.repository.BookOperationalRepository
import com.back.domain.book.repository.BookRepository
import com.back.domain.book.repository.BookViewCountRedisRepository
import com.back.global.rq.Rq
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BookViewCountService(
    private val rq: Rq,
    private val bookRepository: BookRepository,
    private val bookOperationalRepository: BookOperationalRepository,
    private val bookViewCountRedisRepository: BookViewCountRedisRepository,
) {

    fun getBookViewCount(book: Book): Int {
        return bookViewCountRedisRepository
            .findBookViewCountById(book.id)
            ?: getDBBookViewCount(book)
    }

    @Transactional
    fun incrementViewCount(book: Book) {
        if (rq.getCookieValue("viewed-%d".format(book.id), "") == "true") {
            return
        }

        if (!bookViewCountRedisRepository
            .tryIncreaseViewAtRedis(book.id)
            { getDBBookViewCount(book) }) {
            updateBookViewCountInDb(book, getDBBookViewCount(book) + 1)
        }

        rq.setCookie("viewed-%d".format(book.id), "true", 60)
    }

    @Transactional
    fun updateBooksViewCountInDb() {
        val viewMap = bookViewCountRedisRepository.findAllBookViews()

        for (tuple in viewMap) {
            val book = bookRepository.findByIdOrNull(tuple.key) ?: continue
            updateBookViewCountInDb(book, tuple.value)
        }
    }

    private fun updateBookViewCountInDb(book: Book, viewCount: Int) {
        val bookOperational = getBookOperational(book)
            ?: bookOperationalRepository.save(BookOperational(book.isbn))

        bookOperational.viewCount = viewCount
    }

    private fun getBookOperational(book: Book) =
        bookOperationalRepository.findByIsbn(book.isbn)

    private fun getDBBookViewCount(book: Book) =
        getBookOperational(book)?.viewCount ?: 0
}
