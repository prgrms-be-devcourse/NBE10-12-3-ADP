package com.back.domain.book.dto

import com.back.domain.book.entity.Book
import jakarta.validation.constraints.NotNull

class BookDto(
    @JvmField
    val id: Long,
    val title: String,
    val imgUrl: String?,
    val averageRating: Double
) {
    constructor(book: Book) : this(
        book.id,
        book.title,
        book.imgUrl,
        book.averageRating)
}