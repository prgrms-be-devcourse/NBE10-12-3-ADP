package com.back.domain.book.repository;

import com.back.domain.book.entity.BookFetchProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookFetchProgressRepository extends JpaRepository<BookFetchProgress, Long> {
    Optional<BookFetchProgress> findFirstByOrderByIdAsc();
}