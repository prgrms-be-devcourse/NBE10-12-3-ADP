package com.back.domain.book.service

import com.back.domain.book.client.KakaoBookClient
import com.back.domain.book.entity.Book
import com.back.domain.book.repository.BookRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Optional

class BookThumbnailServiceTest {
    private val bookRepository: BookRepository = mock(BookRepository::class.java)
    private val kakaoBookClient: KakaoBookClient = mock(KakaoBookClient::class.java)
    private val bookThumbnailWriter: BookThumbnailWriter = mock(BookThumbnailWriter::class.java)
    private val bookThumbnailService =
        BookThumbnailService(kakaoBookClient, bookThumbnailWriter, bookRepository)

    @Test
    fun `imgUrl이 있으면 외부 썸네일 요청 없이 DB 값을 그대로 반환한다`() {
        val book = Book(
            title = "썸네일 보유 도서",
            description = null,
            isbn = "9790000000001",
            authors = "작가",
            publishedDate = null,
            publisher = "출판사",
            imgUrl = "https://example.com/cover.png",
        )

        `when`(bookRepository.findById(1L)).thenReturn(Optional.of(book))

        val thumbnail = bookThumbnailService.getOrFetchThumbnail(1L)

        assertThat(thumbnail).isEqualTo("https://example.com/cover.png")
        verify(kakaoBookClient, never()).getThumbnailByIsbn(book.isbn)
    }
}
