package com.back.domain.book.service

import com.back.domain.book.client.KakaoBookClient
import com.back.domain.book.repository.BookRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class BookThumbnailService(
    private val bookRepository: BookRepository,
    private val kakaoBookClient: KakaoBookClient,
    private val bookThumbnailWriter: BookThumbnailWriter,
) {

    // 카카오 API 호출(최대 2초)은 트랜잭션 밖에서 수행해 DB 커넥션을 점유하지 않도록 함
    fun fillMissingImgUrl(bookId: Long): String? {
        val book = bookRepository.findByIdOrNull(bookId) ?: return null
        if (!book.imgUrl.isNullOrBlank()) return book.imgUrl

        val thumbnail = kakaoBookClient.getThumbnailByIsbn(book.isbn) ?: return null
        bookThumbnailWriter.updateImgUrl(bookId, thumbnail)

        return thumbnail
    }
}