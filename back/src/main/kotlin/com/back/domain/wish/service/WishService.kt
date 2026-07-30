package com.back.domain.wish.service

import com.back.domain.book.entity.Book
import com.back.domain.member.entity.Member
import com.back.domain.wish.entity.Wish
import com.back.domain.wish.repository.WishRepository
import com.back.global.exception.ServiceException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Service
@Transactional(readOnly = true)
class WishService(
    private val wishRepository: WishRepository
) {
    fun findAll(): List<Wish> = wishRepository.findAll()

    fun findByMember(member: Member): List<Wish> = wishRepository.findByMember(member)

    private fun findByMemberAndBook(member: Member, book: Book): Optional<Wish> =
        wishRepository.findByMemberAndBook(member, book)

    fun addWish(member: Member, book: Book): Wish {
        if (findByMemberAndBook(member, book).isPresent) {
            throw ServiceException("409-1", "이미 존재하는 찜 정보입니다.")
        }
        return wishRepository.save(Wish(member, book))
    }

    fun deleteWish(member: Member, book: Book) {
        val wish = findByMemberAndBook(member, book)
            .orElseThrow { ServiceException("404", "존재하지 않는 찜 정보입니다.") }
        wishRepository.delete(wish)
    }
}
