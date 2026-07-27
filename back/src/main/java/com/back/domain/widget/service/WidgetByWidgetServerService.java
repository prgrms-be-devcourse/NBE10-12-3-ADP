package com.back.domain.widget.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.review.entity.Review;
import com.back.domain.review.service.ReviewService;
import com.back.domain.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WidgetByWidgetServerService {

    record WidgetRenderRequest(
            long reviewCount,
            long reviewWithContentCount,
            int wishCount,
            List<BookItem> books
    ) {
        public record BookItem(
                String title,
                boolean hasContent
        ) {}
    }

    private static final int VISIBLE_BOOK_MAX_COUNT = 5;

    private final MemberService memberService;
    private final ReviewService reviewService;
    private final WishService wishService;
    private final WebClient widgetRendererWebClient;

    public String createWidget(String githubId) {
        Member member = memberService.findByGithubId(githubId);
        List<Review> reviews = reviewService
                .getByMember(member, 0, VISIBLE_BOOK_MAX_COUNT)
                .stream().toList();

        long reviewCount = reviewService.getReviewCountByMember(member);
        long reviewWithContentCount = reviewService.getReviewWithContentCountByMember(member);

        int wishCount = wishService.findByMember(member).size();

        int startIndex = Math.max(0, reviews.size() - VISIBLE_BOOK_MAX_COUNT);

        List<WidgetRenderRequest.BookItem> books = reviews.subList(startIndex, reviews.size())
                .stream()
                .map(review -> new WidgetRenderRequest.BookItem(
                        review.getBook().getTitle(),
                        review.getContent() != null && !review.getContent().isBlank()
                ))
                .toList();

        WidgetRenderRequest request = new WidgetRenderRequest(
                reviewCount,
                reviewWithContentCount,
                wishCount,
                books
        );

        return widgetRendererWebClient.post()
                .uri("/widgets/bookshelf")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.valueOf("image/svg+xml"))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(3));
    }
}