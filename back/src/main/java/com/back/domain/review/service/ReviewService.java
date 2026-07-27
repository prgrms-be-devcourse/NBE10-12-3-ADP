package com.back.domain.review.service;

import com.back.domain.book.entity.Book;
import com.back.domain.book.service.BookService;
import com.back.domain.member.entity.Member;
import com.back.domain.review.entity.Review;
import com.back.domain.review.repository.ReviewRepository;
import com.back.domain.tag.service.TagService;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    private final BookService bookService;
    private final TagService tagService;

    public List<Review> findByBookId(Long bookId) {
        Book book = bookService.getPureBook(bookId);

        return reviewRepository.findByBook(book);
    }

    public Page<Review> getByBookId(Long bookId, int page, int size) {
        Book book = bookService.getPureBook(bookId);
        Pageable pageable = PageRequest.of(page, size);

        return reviewRepository.findByBook(book, pageable);
    }

    public List<Review> findByMember(Member member) {
        return reviewRepository.findByReviewer(member);
    }

    public Page<Review> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return reviewRepository.findAll(pageable);
    }

    public Page<Review> getByMember(Member member, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.findByReviewer(member, pageable);

    }

    @Transactional
    public Review addReview(Long bookId, Member actor, float rating, String comment, List<String> tags) throws ServiceException {
        Book book = bookService.getPureBook(bookId);

        Optional<Review> optionalReview = reviewRepository.findFirstByBookAndReviewer(book, actor);
        if (optionalReview.isPresent()) throw new ServiceException("409-1", "이미 작성한 리뷰가 있습니다.");

        Review review = reviewRepository.save(new Review(book, actor, rating, comment,
                tags.stream().map(tagService::findByNameOrSave).toList()));

        refreshBookRating(book);

        return review;
    }


    public Map<String, Object> getRatingMap(Member member) {
        Map<String, Object> ratings = new LinkedHashMap<>();

        ratings.put("average", Math.round(reviewRepository.getAverageRatingByMember(member) * 10.0f) / 10.0f);

        for (int i = 0; i < 10; i++) {
            float targetRating = (i + 1) * 0.5f;
            ratings.put("%.1f".formatted(targetRating),
                    reviewRepository.countByReviewerAndRating(member, targetRating));
        }

        return ratings;
    }

    public Optional<Review> findLatest() {
        return reviewRepository.findFirstByOrderByIdDesc();
    }

    public Review findById(long id) {
        return reviewRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 리뷰입니다.")
        );
    }

    @Transactional
    public void editReview(Review review, Member reviewer, float rating, String content, List<String> tags) {

        if (!review.getReviewer().equals(reviewer)) {
            throw new ServiceException("403-1", "수정 권한이 없습니다.");
        }

        review.modify(rating, content,
                tags.stream().map(tagService::findByNameOrSave).toList());

        refreshBookRating(review.getBook());
    }

    @Transactional
    public void deleteReview(Review review, Member reviewer) {

        if (!review.getReviewer().equals(reviewer) && !reviewer.isAdmin()) {
            throw new ServiceException("403-1", "삭제 권한이 없습니다.");
        }

        Book book = review.getBook();
        reviewRepository.delete(review);

        refreshBookRating(book);
    }

    private void refreshBookRating(Book book) {
        double averageRating = reviewRepository.getAverageRatingByBook(book);
        int reviewCount = reviewRepository.countByBook(book);
        book.refreshRating(averageRating, reviewCount);
    }

    public List<Review> getPureReviewAll() {
        return reviewRepository.findAll();
    }

    public long getReviewCountByMember(Member member) {
        return reviewRepository.countByReviewer(member);
    }

    public long getReviewWithContentCountByMember(Member member) {
        return reviewRepository.countByReviewerAndContentNot(member, "");
    }
}
