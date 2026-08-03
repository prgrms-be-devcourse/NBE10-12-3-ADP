package com.back.domain.book.service

import com.back.domain.book.client.KakaoBookClient
import com.back.domain.book.repository.BookRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class BookThumbnailService(
    private val bookRepository: BookRepository,
    private val kakaoBookClient: KakaoBookClient,
) {

    // BookService.getBook()의 읽기 전용 트랜잭션 안에서 호출되므로,
    // 실제로 커밋되는 쓰기 트랜잭션을 보장하기 위해 별도 빈 + REQUIRES_NEW로 분리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun fillMissingImgUrl(bookId: Long): String? {
        val book = bookRepository.findByIdOrNull(bookId) ?: return null
        if (!book.imgUrl.isNullOrBlank()) return book.imgUrl

        val thumbnail = kakaoBookClient.getThumbnailByIsbn(book.isbn) ?: return null
        book.updateImgUrl(thumbnail)

        return thumbnail
    }
}