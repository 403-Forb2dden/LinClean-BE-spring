package com.linclean.domain.link.repository;

import com.linclean.domain.link.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByIdAndMember_Id(Long id, Long memberId);
}
