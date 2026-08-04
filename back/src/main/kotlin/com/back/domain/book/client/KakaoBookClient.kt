package com.back.domain.book.client

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Component
class KakaoBookClient(
    private val kakaoBookApiWebClient: WebClient,
) {

    fun getThumbnailByIsbn(isbn: String): String? {
        if (isbn.isBlank()) return null

        return try {
            kakaoBookApiWebClient.get()
                .uri { uriBuilder ->
                    uriBuilder.path("/v3/search/book")
                        .queryParam("query", isbn)
                        .queryParam("target", "isbn")
                        .build()
                }
                .retrieve()
                .bodyToMono(KakaoBookSearchResponse::class.java)
                .block(Duration.ofSeconds(2))
                ?.documents
                ?.firstOrNull()
                ?.thumbnail
                ?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            // 카카오 API 실패는 표지 보강 실패로만 취급하고 도서 조회 자체는 정상 진행되어야 함
            null
        }
    }
}