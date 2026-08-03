package com.back.domain.book.dto

import com.back.domain.book.entity.Book
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class BookDetailDto(
    id: Long,
    title: String,
    imgUrl: String?,
    val description: String?,
    val isbn: String,
    val publishedDate: String,
    val authors: List<String>,
    val publisher: String?,
    val translators: List<String>,
    val reviewCount: Int,
    val rating: Map<String, Any>,
    val tags: List<String>,
    val isWished: Boolean
) : BookDtoBase(id, title, imgUrl) {
    constructor(
        book: Book,
        isWished: Boolean,
        ratingMap: Map<String, Any>,
        tags: List<String>
    ) : this(
        book.id,
        book.title,
        book.imgUrl,
        book.description,
        book.isbn,
        formatPublishedDate(book),
        parseAuthors(book),
        book.publisher,
        listOf<String>(),
        book.reviewCount,
        ratingMap,
        tags,
        isWished
    )

    companion object {
        private fun formatPublishedDate(book: Book): String {
            return book.publishedDate?.toLocalDate().toString() ?: ""
        }

        private fun parseAuthors(book: Book): List<String> {
            if (book.authors.isNullOrBlank()) return listOf()
            return book.authors!!.split(",\\s*")
        }
    }
}