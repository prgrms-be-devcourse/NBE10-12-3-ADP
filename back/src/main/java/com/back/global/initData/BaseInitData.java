package com.back.global.initData;

import com.back.domain.book.entity.Book;
import com.back.domain.book.repository.BookRepository;
import com.back.domain.book.service.BookService;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.review.service.ReviewService;
import com.back.domain.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Profile({"dev", "test"})
@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    @Autowired
    @Lazy
    private BaseInitData self;
    private final MemberService memberService;
    private final BookRepository bookRepository;
    private final WishService wishService;
    private final ReviewService reviewService;
    private final BookService bookService;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            self.work1();
        };
    }

    @Transactional
    public void work1() {
        if (memberService.count() > 0) return;

        Member memberSystem = memberService.join("system", "1234", null, "시스템", null);
        memberSystem.modifyRefreshToken(memberSystem.getUsername());
        memberSystem.grantAdmin();

        Member memberAdmin = memberService.join("admin", "1234", null, "관리자", null);
        memberAdmin.modifyRefreshToken(memberAdmin.getUsername());
        memberAdmin.grantAdmin();

        Member memberUser1 = memberService.join("user1", "1234", "githubuser1", null);
        memberUser1.modifyRefreshToken(memberUser1.getUsername());

        Member memberUser2 = memberService.join("user2", "1234", "githubuser2", null);
        memberUser2.modifyRefreshToken(memberUser2.getUsername());

        Member memberUser3 = memberService.join("user3", "1234", "githubuser3", null);
        memberUser3.modifyRefreshToken(memberUser3.getUsername());

        Book book1 = bookRepository.save(new Book(
                "책제목", "책설명", "isbn1", "작가",
                LocalDateTime.now(), "출판사", ""));
        Book book2 = bookRepository.save(new Book(
                "책제목2", "책설명", "isbn2", "작가",
                LocalDateTime.now(), "출판사", ""));

        Book book3 = bookRepository.save(new Book(
                "책제목3", "책설명", "isbn3", "작가",
                LocalDateTime.now(), "출판사", ""));

        Book book4 = bookRepository.save(new Book(
                "책제목4", "책설명", "isbn4", "작가",
                LocalDateTime.now(), "출판사", ""));

        wishService.addWish(memberUser1, book1);
        wishService.addWish(memberUser2, book2);
        wishService.addWish(memberUser3, book3);

        reviewService.addReview(book1.getId(), memberUser1, 4.0f, "comment", List.of("a", "b"));
        reviewService.addReview(book2.getId(), memberUser1, 3.5f, "", List.of("Java", "Spring", "신입 개발자"));
        reviewService.addReview(book3.getId(), memberUser1, 1.5f, "많이 아쉬운 책입니다..", List.of("소설", "감자"));
        reviewService.addReview(book4.getId(), memberUser1, 4.5f, "", List.of("소설"));

        reviewService.addReview(book2.getId(), memberUser2, 3.5f, "", List.of("Java", "Spring", "신입 개발자"));
        reviewService.addReview(book3.getId(), memberUser2, 1.5f, "많이 아쉬운 책입니다..", List.of("소설", "감자"));
        reviewService.addReview(book4.getId(), memberUser2, 4.5f, "", List.of("소설"));

        reviewService.addReview(book2.getId(), memberUser3, 3.5f, "", List.of("Java", "Spring", "신입 개발자"));
        reviewService.addReview(book3.getId(), memberUser3, 1.5f, "많이 아쉬운 책입니다..", List.of("소설", "감자"));
    }

}