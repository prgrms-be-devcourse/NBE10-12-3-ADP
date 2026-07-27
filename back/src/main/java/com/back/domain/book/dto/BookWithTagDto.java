package com.back.domain.book.dto;

import com.back.domain.book.entity.Book;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BookWithTagDto(
        @NotNull
        Long id,
        @NotNull
        String title,
        @NotNull
        String imgUrl,
        @NotNull
        Double averageRating,
        @NotNull
        List<String> tags
) {
    public BookWithTagDto(Book book, List<String> tags) {
        this(
                book.getId(),
                book.getTitle(),
                book.getImgUrl(),
                book.getAverageRating(),
                tags
        );
    }
}
