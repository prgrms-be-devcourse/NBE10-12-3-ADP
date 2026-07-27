package com.back.domain.wish.repository;

import com.back.domain.book.entity.Book;
import com.back.domain.member.entity.Member;
import com.back.domain.wish.entity.Wish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishRepository extends JpaRepository<Wish, Long> {

    void deleteByMemberAndBook(Member actor, Book book);

    void deleteAllByBook(Book book);

    List<Wish> findByMember(Member actor);

    Optional<Wish> findByMemberAndBook(Member actor, Book book);
}
