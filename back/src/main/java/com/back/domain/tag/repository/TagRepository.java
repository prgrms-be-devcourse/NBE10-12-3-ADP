package com.back.domain.tag.repository;

import com.back.domain.tag.entity.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(@NotBlank String name);
}
