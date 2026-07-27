package com.back.domain.book.controller;

import com.back.domain.book.dto.BookDetailDto;
import com.back.domain.book.dto.BookDto;
import com.back.domain.book.entity.Book;
import com.back.domain.book.service.BookRankService;
import com.back.domain.book.service.BookRecommendService;
import com.back.domain.book.service.BookService;
import com.back.domain.book.service.BookViewsService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "ApiV1BookController", description = "API 도서 컨트롤러")
public class ApiV1BookController {
    private final BookService bookService;
    private final BookRecommendService bookRecommendService;
    private final Rq rq;
    private final BookRankService bookRankService;
    private final BookViewsService bookViewsService;

    @GetMapping("/admin")
    @Operation(summary = "도서 다건 조회 (관리자)")
    @SecurityRequirement(name = "bearerAuth")
    public Page<BookDto> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return bookService.getBooks(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "도서 단건 조회")
    public BookDetailDto getBook(@PathVariable long id) {
        bookViewsService.incrementViewCount(id);
        return bookService.getBook(id, rq.getActor());
    }

    @GetMapping("/search")
    @Operation(summary = "도서 검색")
    public List<BookDto> search(
            @RequestParam @NotBlank String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return bookService.search(searchTerm, page, size);
    }

    @GetMapping("/recommend")
    public List<BookDto> recommend() {
        return bookRecommendService.getRecommends(rq.getActor());

    }

    @GetMapping("/rank")
    public List<BookDto> rank(
            @RequestParam(defaultValue = "") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (type.equals("rating")) return bookRankService.getBooksByRating(page, size);
        if (type.equals("reviewCount")) return bookRankService.getBooksByReviewCnt(page, size);

        return bookViewsService.topViewedInLastHour(page, size);

    }

    public record BookModifyReqBody(
            @NotBlank
            String title,
            String description,
            String authors,
            String publisher,
            String imgUrl
    ) {
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "도서 정보 수정 (관리자)")
    @SecurityRequirement(name = "bearerAuth")
    public RsData<BookDto> modify(
            @PathVariable long id,
            @RequestBody @Valid BookModifyReqBody req
    ) {
        Book book = bookService.editBook(
                id, req.title(), req.description(), req.authors(), req.publisher(), req.imgUrl());

        return new RsData<>("200-1", "도서 정보 수정 완료", new BookDto(book));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "도서 삭제 (관리자)")
    @SecurityRequirement(name = "bearerAuth")
    public RsData<Void> delete(
            @PathVariable long id
    ) {
        bookService.deleteBook(id);

        return new RsData<>("200-1", "도서 삭제 완료");
    }
}
