package com.back.domain.wish.repository

import com.back.domain.book.entity.Book
import com.back.domain.member.entity.Member
import com.back.domain.wish.entity.Wish
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface WishRepository : JpaRepository<Wish, Long> {

    fun deleteByMemberAndBook(member: Member, book: Book)

    fun deleteAllByBook(book: Book)

    fun findByMember(member: Member): List<Wish>

    fun findByMemberAndBook(member: Member, book: Book): Optional<Wish>
}
