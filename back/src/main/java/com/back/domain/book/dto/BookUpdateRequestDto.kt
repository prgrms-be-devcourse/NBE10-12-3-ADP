package com.back.domain.book.dto

class BookUpdateRequestDto(
    val title: String,
    val description: String?,
    val authors: String?,
    val publisher: String?,
    val imgUrl: String?
)