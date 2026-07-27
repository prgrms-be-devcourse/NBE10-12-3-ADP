package com.back.domain.review.repository;

import com.back.domain.book.entity.Book;

public interface BookInterface {
    Book getBook();
    Integer getCnt();
}