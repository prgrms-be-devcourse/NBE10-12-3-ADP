package com.back.domain.book.entity

import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import kotlin.math.roundToInt

@Entity
class BookOperational(
    val bookId: Long,
    var viewCount: Int = 0,
    var averageRating: Double = 0.0,
    var reviewCount: Int = 0

) : BaseEntity() {

    // 리뷰 등록/수정/삭제 후 실제 리뷰 데이터를 기준으로 평균 별점, 리뷰 수 갱신
    fun updateRating(averageRating: Double, reviewCount: Int) {
        this.reviewCount = reviewCount
        this.averageRating = if (reviewCount == 0) 0.0 else (averageRating * 10.0).roundToInt() / 10.0
    }
}