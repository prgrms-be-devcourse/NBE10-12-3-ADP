package com.back.domain.widget.service

import com.back.domain.member.service.MemberService
import com.back.domain.review.repository.ReviewRepository
import com.back.domain.wish.repository.WishRepository
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Service
@Transactional(readOnly = true)
class WidgetByWidgetServerService(
    private val memberService: MemberService,
    private val reviewRepository: ReviewRepository,
    private val widgetRendererWebClient: WebClient,
    private val wishRepository: WishRepository,
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
        val member = memberService.getMemberByGithubId(githubId)
        val reviews = reviewRepository.findByReviewer(member, PageRequest.of(0, VISIBLE_BOOK_MAX_COUNT)).content
        val reviewCount = reviewRepository.countByReviewer(member).toLong()
        val reviewWithContentCount = reviewRepository.countByReviewerAndContentNot(member, "").toLong()

        val wishCount = wishRepository.findByMember(member).size

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
