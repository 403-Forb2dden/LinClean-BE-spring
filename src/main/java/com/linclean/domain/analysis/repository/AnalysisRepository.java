package com.linclean.domain.analysis.repository;

import com.linclean.domain.analysis.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {

    @Query("SELECT a.verdict, COUNT(a) FROM Analysis a WHERE a.verdict IS NOT NULL GROUP BY a.verdict")
    List<Object[]> countGroupByVerdict();
}
