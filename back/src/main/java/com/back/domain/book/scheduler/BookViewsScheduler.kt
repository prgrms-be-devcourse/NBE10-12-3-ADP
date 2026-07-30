package com.back.domain.book.scheduler

import com.back.domain.book.service.BookService
import org.springframework.scheduling.annotation.Scheduled

@org.springframework.stereotype.Component
class BookViewsScheduler(
    private val bookService: BookService

) {

    @Scheduled(cron = "0 */5 * * * *")
    fun syncViewCountsToDb() {
        bookService.updateBooksViewCountInDb()
    }
}
