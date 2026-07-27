package com.back.domain.review.repository;

import com.back.domain.book.entity.Book;
import com.back.domain.member.entity.Member;
import com.back.domain.review.entity.Review;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBook(Book book);
    Page<Review> findByBook(Book book, Pageable pageable);

    List<Review> findByReviewer(Member member);
    Page<Review> findByReviewer(Member member, Pageable pageable);

    int countByReviewerAndRating(Member member, float rating);
    int countByReviewer(Member member);
    int countByReviewerAndContentNot(Member member, String content);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.reviewer = :member")
    double getAverageRatingByMember(Member member);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.book = :book")
    double getAverageRatingByBook(@Param("book") Book book);

    int countByBook(Book book);

    Optional<Review> findFirstByOrderByIdDesc();

    int countByBookAndRating(Book book, float rating);

    Optional<Review> findFirstByBookAndReviewer(Book book, Member reviewer);

    @Query("""
            SELECT r.book AS book, count(*) as CNT
            FROM Review r
            WHERE r.modifiedDate >= :cutoffDate
            GROUP BY r.book
            ORDER BY CNT DESC
           """)
    Page<BookInterface> findBookByOrderByReviewCnt(@Param("cutoffDate") LocalDateTime cutoffDate, Pageable pageable);
}
