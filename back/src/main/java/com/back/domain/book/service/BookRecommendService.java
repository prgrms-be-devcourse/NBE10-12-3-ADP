package com.back.domain.book.service;

import com.back.domain.book.dto.BookDto;
import com.back.domain.member.entity.Member;
import com.back.domain.review.entity.Review;
import com.back.domain.review.service.ReviewService;
import com.back.standard.recommend.byRating.SimilarityRecommendByRating;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.back.standard.recommend.byRating.SimilarityRecommendByRating.Rating;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookRecommendService {
    private final ReviewService reviewService;
    private final BookService bookService;

    private Rating reviewToRecommendReview(Review review) {
        return new Rating(
                review.getReviewer().getId(),
                review.getBook().getId(),
                review.getRating());
    }

    private List<Rating> recommendReviewsByReviewer(Member reviewer) {

        return reviewService.getByMember(reviewer, 0, 5)
                .stream()
                .map(this::reviewToRecommendReview)
                .toList();

    }

    public List<BookDto> getRecommends(Member actor) {

        SimilarityRecommendByRating recommendSystem = new SimilarityRecommendByRating();

        List<Review> recentReviews = reviewService
                .getByMember(actor, 0, 5)
                .stream()
                .toList();

        recommendSystem.setData(
                recommendReviewsByReviewer(actor));

        Set<Member> members = new HashSet<>();

        for (var review : recentReviews)
            reviewService.getByBookId(review.getBook().getId(), 0, 10)
                    .stream()
                    .forEach(r -> members.add(r.getReviewer()));

        for (var reviewer: members) {
            recommendSystem.setData(
                    recommendReviewsByReviewer(reviewer));
        }

        return recommendSystem.getRecommendList(actor.getId(), 5, 10)
                .stream()
                .map(id -> new BookDto(bookService.getPureBook(id)))
                .toList();
    }

}
