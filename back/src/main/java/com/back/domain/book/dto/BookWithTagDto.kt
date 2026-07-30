package com.back.domain.book.dto

import com.back.domain.book.entity.Book
import jakarta.validation.constraints.NotNull

class BookWithTagDto(
    val id: Long,
    val title: String,
    val imgUrl: String?,
    val averageRating: Double,
    val tags: List<String>
) {
    constructor(book: Book, tags: List<String>) : this(
        book.id,
        book.title,
        book.imgUrl,
        book.averageRating,
        tags
    )
}
