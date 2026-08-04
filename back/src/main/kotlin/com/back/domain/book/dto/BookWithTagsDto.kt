package com.back.domain.book.dto

import com.back.domain.book.entity.Book

class BookWithTagsDto(
    book: Book,
    averageRating: Double,
    val tags: List<String>,
) : BookDto(book, averageRating)
