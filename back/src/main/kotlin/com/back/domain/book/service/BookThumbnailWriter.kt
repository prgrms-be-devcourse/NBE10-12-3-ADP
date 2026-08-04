package com.back.domain.book.service

import com.back.domain.book.repository.BookRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class BookThumbnailWriter(
    private val bookRepository: BookRepository,
) {

    // BookThumbnailService와 별도의 빈으로 분리해야 @Transactional 프록시가 실제로 적용됨
    // (같은 빈 안에서 self-invocation하면 프록시를 안 거쳐 트랜잭션이 무시됨)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun updateImgUrl(bookId: Long, thumbnail: String) {
        val book = bookRepository.findByIdOrNull(bookId) ?: return
        if (!book.imgUrl.isNullOrBlank()) return

        book.updateImgUrl(thumbnail)
    }
}