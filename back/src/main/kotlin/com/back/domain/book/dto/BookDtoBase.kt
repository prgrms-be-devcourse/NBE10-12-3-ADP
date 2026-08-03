package com.back.domain.book.dto

open class BookDtoBase(
    @JvmField
    val id: Long,
    val title: String,
    val imgUrl: String?
)