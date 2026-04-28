package com.linclean.domain.analysis.service;

import com.linclean.domain.analysis.dto.request.AnalysisRequest;
import com.linclean.domain.analysis.dto.response.AnalysisResponse;
import com.linclean.domain.analysis.entity.Analysis;
import com.linclean.domain.analysis.entity.AnalysisReason;
import com.linclean.domain.analysis.exception.AnalysisException;
import com.linclean.domain.analysis.repository.AnalysisReasonRepository;
import com.linclean.domain.analysis.repository.AnalysisRepository;
import com.linclean.domain.member.entity.Member;
import com.linclean.domain.member.repository.MemberRepository;
import com.linclean.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final AnalysisReasonRepository analysisReasonRepository;
    private final MemberRepository memberRepository;
    private final AnalysisAsyncRunner asyncRunner;

    @Transactional
    public AnalysisResponse requestAnalysis(Long memberId, AnalysisRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AnalysisException(ErrorCode.MEMBER_NOT_FOUND));

        UUID requestId = UUID.randomUUID();

        Analysis analysis = Analysis.builder()
                .member(member)
                .originalUrl(request.url())
                .requestId(requestId)
                .build();

        Analysis saved = analysisRepository.save(analysis);
        UUID savedAnalysisId = saved.getAnalysisId();
        String savedUrl = saved.getOriginalUrl();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                asyncRunner.run(savedAnalysisId, savedUrl, requestId);
            }
        });

        return AnalysisResponse.forQueued(saved.getAnalysisId());
    }

    @Transactional(readOnly = true)
    public AnalysisResponse getAnalysis(Long memberId, UUID analysisId) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new AnalysisException(ErrorCode.ANALYSIS_NOT_FOUND));

        if (!analysis.getMember().getId().equals(memberId)) {
            throw new AnalysisException(ErrorCode.ANALYSIS_FORBIDDEN);
        }

        return switch (analysis.getStatus()) {
            case QUEUED -> AnalysisResponse.forQueued(analysisId);
            case SUCCEEDED -> {
                List<AnalysisReason> reasons =
                        analysisReasonRepository.findAllByAnalysis_AnalysisId(analysisId);
                yield AnalysisResponse.forSucceeded(analysis, reasons);
            }
            case FAILED -> AnalysisResponse.forFailed(analysis);
        };
    }
}
