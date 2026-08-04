package com.back.domain.book.service

import com.back.domain.book.client.KakaoBookClient
import org.springframework.stereotype.Service

@Service
class BookThumbnailService(
    private val kakaoBookClient: KakaoBookClient,
    private val bookThumbnailWriter: BookThumbnailWriter,
) {

    // 카카오 API 호출(최대 2초)은 트랜잭션 밖에서 수행해 DB 커넥션을 점유하지 않도록 함
    // 호출부에서 이미 Book을 조회해 imgUrl 여부를 확인했으므로, 중복 조회 없이 isbn을 직접 전달받음
    fun fillMissingImgUrl(bookId: Long, isbn: String): String? {
        val thumbnail = kakaoBookClient.getThumbnailByIsbn(isbn)
        if (thumbnail == null) {
            bookThumbnailWriter.markFetchAttempted(bookId)
            return null
        }

        bookThumbnailWriter.updateImgUrl(bookId, thumbnail)

        return thumbnail
    }
}