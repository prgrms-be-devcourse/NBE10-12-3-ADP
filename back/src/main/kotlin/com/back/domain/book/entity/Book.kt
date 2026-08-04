package com.back.domain.book.entity

import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime
import kotlin.math.roundToInt

@Entity
@Table(
    indexes = [
        Index(name = "idx_book_average_rating", columnList = "averageRating"),
        Index(name = "idx_book_review_count", columnList = "reviewCount")]
)
class Book(
    @field:Column(nullable = false, columnDefinition = "LONGTEXT")
    var title: String,

    @field:Column(columnDefinition = "LONGTEXT")
    var description: String?,

    @field:Column(unique = true)
    var isbn: String,

    @field:Column(columnDefinition = "LONGTEXT")
    var authors: String?,

    val publishedDate: LocalDateTime?,
    var publisher: String?,
    var imgUrl: String?

) : BaseEntity() {

    fun update(title: String, description: String?, authors: String?, publisher: String?, imgUrl: String?) {
        this.title = title
        this.description = description
        this.authors = authors
        this.publisher = publisher
        this.imgUrl = imgUrl
    }

    fun updateImgUrl(imgUrl: String) {
        this.imgUrl = imgUrl
    }
}
