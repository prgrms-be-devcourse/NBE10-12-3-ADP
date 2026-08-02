package com.back.domain.wish.service

import com.back.domain.book.dto.BookWithTagsDto
import com.back.domain.book.repository.BookRepository
import com.back.domain.member.entity.Member
import com.back.domain.review.repository.ReviewRepository
import com.back.domain.wish.entity.Wish
import com.back.domain.wish.repository.WishRepository
import com.back.global.exception.ServiceException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class WishService(
    private val wishRepository: WishRepository,
    private val reviewRepository: ReviewRepository,
    private val bookRepository: BookRepository,
) {

    fun getMyWishes(actor: Member): List<BookWithTagsDto> =
        wishRepository.findByMember(actor).map { wish ->
            BookWithTagsDto(
                wish.book,
                reviewRepository.findByBook(wish.book)
                    .flatMap { review -> review.tags }
                    .distinct(),
            )
        }

    @Transactional
    fun createWish(actor: Member, bookId: Long) {
        val book = bookRepository.findById(bookId).orElseThrow {
            ServiceException("404-1", "존재하지 않는 도서입니다.")
        }

        wishRepository.findByMemberAndBook(actor, book)?.let {
            throw ServiceException("409-1", "이미 존재하는 찜입니다.")
        }

        wishRepository.save(Wish(actor, book))
    }

    @Transactional
    fun deleteWish(actor: Member, id: Long) {
        val wish = wishRepository.findByIdOrNull(id) ?: throw ServiceException("404-1", "존재하지 않는 찜입니다.")

        if (actor != wish.member)
            throw ServiceException("403-1", "찜 삭제 권한이 없습니다.")

        wishRepository.delete(wish)
    }
}
