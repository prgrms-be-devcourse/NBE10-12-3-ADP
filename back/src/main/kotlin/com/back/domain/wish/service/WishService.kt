package com.back.domain.wish.service

import com.back.domain.book.entity.Book
import com.back.domain.member.entity.Member
import com.back.domain.wish.entity.Wish
import com.back.domain.wish.repository.WishRepository
import com.back.global.exception.ServiceException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class WishService(
    private val wishRepository: WishRepository
) {
    fun findAll(): List<Wish> = wishRepository.findAll()

    fun getWishesByMember(member: Member): List<Wish> = wishRepository.findByMember(member)

    fun addWish(member: Member, book: Book): Wish {
        if (wishRepository.findByMemberAndBook(member, book).isPresent) {
            throw ServiceException("409-1", "이미 존재하는 찜 정보입니다.")
        }
        return wishRepository.save(Wish(member, book))
    }

    fun oldDeleteWish(member:Member, book: Book) {
        val wish = wishRepository.findByMemberAndBook(member, book)
            .orElseThrow { ServiceException("404", "존재하지 않는 찜 정보입니다.") }

        if (member != wish.member)
            throw ServiceException("403-1", "찜 삭제 권한이 없습니다.")

        wishRepository.delete(wish)

    }

    fun deleteWish(member: Member, id: Long) {
        val wish = wishRepository.findById(id)
            .orElseThrow { ServiceException("404-1", "존재하지 않는 찜 정보입니다.") }

        if (member != wish.member)
            throw ServiceException("403-1", "찜 삭제 권한이 없습니다.")

        wishRepository.delete(wish)
    }
}
