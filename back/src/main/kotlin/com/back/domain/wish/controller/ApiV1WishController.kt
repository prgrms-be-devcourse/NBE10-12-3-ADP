package com.back.domain.wish.controller

import com.back.domain.book.dto.BookWithTagDto
import com.back.domain.book.service.BookService
import com.back.domain.wish.service.WishService
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/wishes")
@Tag(name = "ApiV1WishController", description = "API 찜 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
@Transactional(readOnly = true)
class ApiV1WishController(
    private val wishService: WishService,
    private val bookService: BookService,
    private val rq: Rq
) {

    @GetMapping("/mine")
    @Operation(summary = "내 찜 목록 조회")
    fun getWishes(): List<BookWithTagDto> {
        val actor = rq.actorFromDb

        return wishService
            .getWishesByMember(actor)
            .map { BookWithTagDto(it.book, bookService.getBookTags(it.book)) }
    }

    @PostMapping("/book/{id}")
    @Operation(summary = "찜 목록 추가")
    @Transactional
    fun addWish(
        @PathVariable @Valid id: Long
    ): RsData<Void> {
        val book = bookService.getBook(id)

        val actor = rq.actorFromDb
        wishService.addWish(actor, book)

        return RsData("201-1", "찜 생성을 성공했습니다.")
    }

    @DeleteMapping("/book/{id}")
    @Operation(summary = "찜 목록 삭제 (구형)")
    @Transactional
    fun oldDeleteWish(
        @PathVariable @Valid id: Long
    ): RsData<Void> {
        val actor = rq.actorFromDb
        val book = bookService.getBookById(id)
        wishService.oldDeleteWish(actor, book)

        return RsData("200-1", "찜 삭제를 성공했습니다.")
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "찜 목록 삭제")
    @Transactional
    fun deleteWish(
        @PathVariable @Valid id: Long
    ): RsData<Void> {
        val actor = rq.actorFromDb
        wishService.deleteWish(actor, id)

        return RsData("200-1", "찜 삭제를 성공했습니다.")
    }
}
