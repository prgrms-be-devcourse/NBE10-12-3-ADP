package com.back.domain.review.entity;


import com.back.domain.tag.entity.Tag;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class ReviewTag extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY)
    private Review review;

    @ManyToOne(fetch= FetchType.LAZY)
    private Tag tag;

    public ReviewTag(Review review, Tag tag) {
        this.review = review;
        this.tag = tag;
    }
}
