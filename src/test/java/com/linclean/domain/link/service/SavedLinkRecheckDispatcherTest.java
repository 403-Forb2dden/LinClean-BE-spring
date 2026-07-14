package com.linclean.domain.link.service;

import com.linclean.domain.analysis.entity.Analysis;
import com.linclean.domain.analysis.entity.AnalysisStatus;
import com.linclean.domain.analysis.entity.Verdict;
import com.linclean.domain.analysis.repository.AnalysisRepository;
import com.linclean.domain.analysis.service.AnalysisAsyncRunner;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.eq;

@ExtendWith(MockitoExtension.class)
class SavedLinkRecheckDispatcherTest {

    @Mock AnalysisRepository analysisRepository;
    @Mock AnalysisAsyncRunner asyncRunner;
    @InjectMocks SavedLinkRecheckDispatcher dispatcher;

    private void runCommitCallbacks() {
        for (TransactionSynchronization s : TransactionSynchronizationManager.getSynchronizations()) {
            s.afterCommit();
        }
    }

    @Nested
    class dispatch {

        @Test
        void 대상을_QUEUED로_리셋하고_커밋후_기존_위임로직으로_재검사를_요청한다() {
            UUID analysisId = UUID.randomUUID();
            UUID oldRequestId = UUID.randomUUID();
            Analysis analysis = Analysis.builder()
                    .analysisId(analysisId)
                    .originalUrl("https://example.com")
                    .status(AnalysisStatus.SUCCEEDED)
                    .verdict(Verdict.SAFE)
                    .requestId(oldRequestId)
                    .build();
            given(analysisRepository.findById(analysisId)).willReturn(Optional.of(analysis));

            TransactionSynchronizationManager.initSynchronization();
            try {
                dispatcher.dispatch(analysisId);

                // 커밋 전: 상태만 재검사용으로 리셋되고 아직 위임하지 않는다
                assertThat(analysis.getStatus()).isEqualTo(AnalysisStatus.QUEUED);
                assertThat(analysis.getRequestId()).isNotEqualTo(oldRequestId);
                assertThat(analysis.getLastCheckedAt()).isNotNull();
                assertThat(analysis.getVerdict()).isEqualTo(Verdict.SAFE); // 이전 등급은 콜백 전까지 유지
                then(asyncRunner).shouldHaveNoInteractions();

                runCommitCallbacks();
            } finally {
                TransactionSynchronizationManager.clearSynchronization();
            }

            then(asyncRunner).should()
                    .run(eq(analysisId), eq("https://example.com"), eq(analysis.getRequestId()));
        }

        @Test
        void 직전이_FAILED였던_분석은_재검사시_오류_필드가_초기화된다() {
            UUID analysisId = UUID.randomUUID();
            Analysis analysis = Analysis.builder()
                    .analysisId(analysisId)
                    .originalUrl("https://example.com")
                    .status(AnalysisStatus.FAILED)
                    .requestId(UUID.randomUUID())
                    .errorCode("ENGINE_REQUEST_ERROR")
                    .errorStage(2)
                    .errorMessage("이전 재검사 실패")
                    .build();
            given(analysisRepository.findById(analysisId)).willReturn(Optional.of(analysis));

            TransactionSynchronizationManager.initSynchronization();
            try {
                dispatcher.dispatch(analysisId);
            } finally {
                TransactionSynchronizationManager.clearSynchronization();
            }

            assertThat(analysis.getStatus()).isEqualTo(AnalysisStatus.QUEUED);
            assertThat(analysis.getErrorCode()).isNull();
            assertThat(analysis.getErrorStage()).isNull();
            assertThat(analysis.getErrorMessage()).isNull();
        }

        @Test
        void 대상_분석이_없으면_위임을_등록하지_않는다() {
            UUID analysisId = UUID.randomUUID();
            given(analysisRepository.findById(analysisId)).willReturn(Optional.empty());

            TransactionSynchronizationManager.initSynchronization();
            try {
                dispatcher.dispatch(analysisId);
                assertThat(TransactionSynchronizationManager.getSynchronizations()).isEmpty();
            } finally {
                TransactionSynchronizationManager.clearSynchronization();
            }

            then(asyncRunner).shouldHaveNoInteractions();
        }
    }
}