package com.back.domain.review.repository

import com.back.domain.book.entity.Book

interface BookInterface {
    val book: Book?
    val cnt: Int?
}