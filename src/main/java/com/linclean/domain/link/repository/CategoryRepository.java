package com.linclean.domain.link.repository;

import com.linclean.domain.link.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByIdAndMember_Id(Long id, Long memberId);

    boolean existsByMember_IdAndName(Long memberId, String name);

    long countByMember_Id(Long memberId);

    List<Category> findAllByMember_IdOrderByDisplayOrderAscCreatedAtAsc(Long memberId);
}
