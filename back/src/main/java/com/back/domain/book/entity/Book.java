package com.back.domain.book.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(indexes = {
        @Index(name = "idx_book_average_rating", columnList = "averageRating"),
        @Index(name = "idx_book_review_count", columnList = "reviewCount")
})
public class Book extends BaseEntity {
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Column(unique = true)
    private String isbn;

    @Column(columnDefinition = "LONGTEXT")
    private String authors;

    private LocalDateTime publishedDate;

    private String publisher;

    private String imgUrl;

    private double averageRating = 0.0;

    private int reviewCount = 0;

    @Setter
    private int viewCount = 0;

    public Book(String title, String description, String isbn,
                String authors, LocalDateTime publishedDate,
                String publisher, String imgUrl) {
        this.title = title;
        this.description = description;
        this.isbn = isbn;
        this.authors = authors;
        this.publishedDate = publishedDate;
        this.publisher = publisher;
        this.imgUrl = imgUrl;
    }

    // 리뷰 등록/수정/삭제 후 실제 리뷰 데이터를 기준으로 평균 별점, 리뷰 수 갱신
    public void refreshRating(double averageRating, int reviewCount) {
        this.reviewCount = reviewCount;
        this.averageRating = reviewCount == 0 ? 0.0 : Math.round(averageRating * 10.0) / 10.0;
    }

    public void modify(String title, String description, String authors, String publisher, String imgUrl) {
        this.title = title;
        this.description = description;
        this.authors = authors;
        this.publisher = publisher;
        this.imgUrl = imgUrl;
    }
}
