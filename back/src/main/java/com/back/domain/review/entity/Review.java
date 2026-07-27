package com.back.domain.review.entity;

import com.back.domain.book.entity.Book;
import com.back.domain.member.entity.Member;
import com.back.domain.tag.entity.Tag;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class Review extends BaseEntity {
    private float rating;
    @Column(length = 500)
    private String content;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private Member reviewer;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewTag> reviewTags = new ArrayList<>();

    public Review(Book book, Member reviewer, float rating, String content, List<Tag> tags) {
        this.book = book;
        this.reviewer = reviewer;
        this.rating = rating;
        this.content = content;

        for (int i = 0; i < tags.size(); i++) {
            this.reviewTags.add(new ReviewTag(this, tags.get(i)));
        }
    }

    public List<String> getTags() {
        return reviewTags.stream()
                .map(tag -> tag.getTag().getName())
                .toList();
    }

    public void modify(float rating, String content, List<Tag> tags) {
        this.rating = rating;
        this.content = content;

        this.reviewTags.clear();
        for (int i = 0; i < tags.size(); i++) {
            this.reviewTags.add(new ReviewTag(this, tags.get(i)));
        }
    }
}
