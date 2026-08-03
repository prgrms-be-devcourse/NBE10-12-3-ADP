package com.back.domain.book.dto

import com.back.domain.book.entity.Book

class BookWithTagsDto(
    book: Book,
    val tags: List<String>,
) : BookDto(book)
