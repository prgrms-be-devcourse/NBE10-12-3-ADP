package com.back.domain.widget.service

import com.back.domain.member.service.MemberService
import com.back.domain.review.service.ReviewService
import com.back.domain.wish.service.WishService
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Service
@Transactional(readOnly = true)
class WidgetByWidgetServerService(
    private val memberService: MemberService,
    private val reviewService: ReviewService,
    private val wishService: WishService,
    private val widgetRendererWebClient: WebClient
) {

    private companion object {
        const val VISIBLE_BOOK_MAX_COUNT = 5
    }

    private class WidgetRenderRequest(
        val reviewCount: Long,
        val reviewWithContentCount: Long,
        val wishCount: Int,
        val books: List<BookItem>
    ) {
        class BookItem(
            val title: String,
            val hasContent: Boolean
        )
    }

    fun createWidget(githubId: String): String? {
        val member = memberService.getByGithubId(githubId)
        val reviews = reviewService.getByMember(member.id, 0, VISIBLE_BOOK_MAX_COUNT).content
        val reviewCount = reviewService.getReviewCountByMember(member.id)
        val reviewWithContentCount = reviewService.getReviewWithContentCountByMember(member.id)

        val wishCount = wishService.findByMember(member).size

        val startIndex = maxOf(0, reviews.size - VISIBLE_BOOK_MAX_COUNT)
        val books = reviews.subList(startIndex, reviews.size).map { review ->
            WidgetRenderRequest.BookItem(
                title = review.book.title,
                hasContent = review.content.isNotBlank()
            )
        }

        val request = WidgetRenderRequest(
            reviewCount = reviewCount,
            reviewWithContentCount = reviewWithContentCount,
            wishCount = wishCount,
            books = books
        )

        return widgetRendererWebClient.post()
            .uri("/widgets/bookshelf")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.valueOf("image/svg+xml"))
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String::class.java)
            .block(Duration.ofSeconds(3))
    }
}
