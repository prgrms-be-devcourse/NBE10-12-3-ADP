package com.back.domain.wish.service;

import com.back.domain.book.entity.Book;
import com.back.domain.member.entity.Member;
import com.back.domain.wish.entity.Wish;
import com.back.domain.wish.repository.WishRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WishService {
    private final WishRepository wishRepository;

    public List<Wish> findAll() {
        return wishRepository.findAll();
    }

    public List<Wish> findByMember(Member actor) {
        return wishRepository.findByMember(actor);
    }

    private Optional<Wish> findByMemberAndBook(Member actor, Book book) {
        return wishRepository.findByMemberAndBook(actor, book);
    }

    public Wish addWish(Member actor, Book book) {
        Optional<Wish> wish = findByMemberAndBook(actor, book);
        if (wish.isPresent()) {
            throw new ServiceException("409-1", "이미 존재하는 찜 정보입니다.");
        }
        return wishRepository.save(new Wish(actor, book));
    }

    public void deleteWish(Member actor, Book book) {
        Optional<Wish> wish = findByMemberAndBook(actor, book);
        wishRepository.delete(wish.orElseThrow(
                () -> new ServiceException("404", "존재하지 않는 찜 정보입니다.")));
    }
}
