package com.back.domain.book.dto;

import com.back.domain.book.entity.Book;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookDetailDto(
        @NotNull
        Long id,
        @NotNull
        String title,
        @NotNull
        String description,
        @NotNull
        String isbn,
        @NotNull
        String publishedDate,
        @NotNull
        List<String> authors,
        @NotNull
        String publisher,
        @NotNull
        List<String> translators,
        @NotNull
        String imgUrl,
        @NotNull
        Integer reviewCount,
        @NotNull
        Map<String, Object> rating,
        @NotNull
        List<String> tags,
        @NotNull
        Boolean isWished
) {
    public BookDetailDto(Book book, Boolean isWished, Map<String, Object> ratingMap, List<String> tags) {
        this(
                book.getId(),
                book.getTitle(),
                book.getDescription(),
                book.getIsbn(),
                formatPublishedDate(book),
                parseAuthors(book),
                book.getPublisher(),
                List.of(),
                book.getImgUrl(),
                book.getReviewCount(),
                ratingMap,
                tags,
                isWished
        );
    }

    private static String formatPublishedDate(Book book) {
        if (book.getPublishedDate() == null) return "";
        return book.getPublishedDate().toLocalDate().toString();
    }

    private static List<String> parseAuthors(Book book) {
        if (book.getAuthors() == null || book.getAuthors().isBlank()) return List.of();
        return List.of(book.getAuthors().split(",\\s*"));
    }
}