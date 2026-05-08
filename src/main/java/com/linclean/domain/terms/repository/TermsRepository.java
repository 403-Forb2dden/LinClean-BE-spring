package com.linclean.domain.terms.repository;

import com.linclean.domain.terms.entity.Terms;
import com.linclean.domain.terms.entity.TermsType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermsRepository extends JpaRepository<Terms, Long> {
    Optional<Terms> findByType(TermsType type);
}