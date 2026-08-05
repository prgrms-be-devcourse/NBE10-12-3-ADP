package com.back.domain.book.service

import com.back.domain.book.client.KakaoBookClient
import com.back.domain.book.repository.BookRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class BookThumbnailService(
    private val kakaoBookClient: KakaoBookClient,
    private val bookThumbnailWriter: BookThumbnailWriter,
    private val bookRepository: BookRepository,
) {

    fun getOrFetchThumbnail(bookId: Long): String? {
        val book = bookRepository.findByIdOrNull(bookId) ?: return null

        if (!book.imgUrl.isNullOrBlank()) {
            return book.imgUrl
        }

        val thumbnail = kakaoBookClient.getThumbnailByIsbn(book.isbn)
        if (thumbnail == null) {
            bookThumbnailWriter.markFetchAttempted(bookId)
            return null
        }

        bookThumbnailWriter.updateImgUrl(bookId, thumbnail)

        return thumbnail
    }
}
