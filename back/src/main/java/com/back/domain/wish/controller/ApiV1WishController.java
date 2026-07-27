package com.back.domain.wish.controller;

import com.back.domain.book.dto.BookDto;
import com.back.domain.book.dto.BookWithTagDto;
import com.back.domain.book.entity.Book;
import com.back.domain.book.repository.BookRepository;
import com.back.domain.book.service.BookService;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.tag.service.TagService;
import com.back.domain.wish.service.WishService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishes")
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Tag(name = "ApiV1WishController", description = "API 찜 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
public class ApiV1WishController {

    private final WishService wishService;
    private final MemberService memberService;
    private final BookService bookService;

    private final Rq rq;
    private final TagService tagService;

    @GetMapping("/mine")
    @Operation(summary = "내 찜 목록 조회")
    public List<BookWithTagDto> getWishes() {
        Member actor = memberService.findById(rq.getActor().getId());

        return wishService
                .findByMember(actor)
                .stream()
                .map(wish -> new BookWithTagDto(wish.getBook(), bookService.getBookTags(wish.getBook())))
                .toList();
    }

    @PostMapping("/book/{id}")
    @Operation(summary = "찜 목록 추가")
    @Transactional
    public RsData<Void> addWish(
            @PathVariable @Valid long id
    ) {

        Book book = bookService.getPureBook(id);

        wishService.addWish(
                rq.getActorFromDb(),
                book);

        return new RsData<>(
                "201-1",
                "찜 추가 성공"
        );
    }

    @DeleteMapping("/book/{id}")
    @Transactional
    @Operation(summary = "찜 목록 삭제")
    public RsData<Void> deleteWish(
            @PathVariable @Valid long id
    ) {

        Book book = bookService.getPureBook(id);

        wishService.deleteWish(
                rq.getActorFromDb(),
                book);

        return new RsData<>(
                "200-1",
                "찜 삭제 성공"
        );
    }

}
