package com.back.domain.book.dto;

import com.back.domain.book.entity.Book;
import jakarta.validation.constraints.NotNull;

public record BookDto(
        @NotNull
        Long id,
        @NotNull
        String title,
        @NotNull
        String imgUrl,
        @NotNull
        Double averageRating
) {
    public BookDto(Book book) {
        this(book.getId(), book.getTitle(), book.getImgUrl(), book.getAverageRating());
    }

}