package com.linclean.domain.analysis.repository;

import com.linclean.domain.analysis.entity.AnalysisReason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnalysisReasonRepository extends JpaRepository<AnalysisReason, Long> {
    List<AnalysisReason> findAllByAnalysis_AnalysisId(UUID analysisId);
}
