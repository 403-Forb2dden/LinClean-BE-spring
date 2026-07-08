package com.linclean.domain.link.service;

import com.linclean.domain.analysis.entity.Analysis;
import com.linclean.domain.analysis.repository.AnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class SavedLinkRecheckServiceTest {

    private static final UUID START_CURSOR = new UUID(0L, 0L);

    @Mock AnalysisRepository analysisRepository;
    @Mock SavedLinkRecheckDispatcher dispatcher;
    SavedLinkRecheckService service;

    @BeforeEach
    void setUp() {
        service = new SavedLinkRecheckService(analysisRepository, dispatcher);
        ReflectionTestUtils.setField(service, "staleDays", 7);
    }

    private Analysis analysis(UUID id) {
        return Analysis.builder()
                .analysisId(id)
                .originalUrl("https://" + id + ".com")
                .build();
    }

    private Slice<Analysis> slice(List<Analysis> content, boolean hasNext) {
        return new SliceImpl<>(content, PageRequest.of(0, 100), hasNext);
    }

    @Nested
    class runRecheck {

        @Test
        void 여러_배치를_순회하며_각_분석을_재검사에_위임한다() {
            UUID a1 = UUID.randomUUID();
            UUID a2 = UUID.randomUUID();
            UUID a3 = UUID.randomUUID();
            given(analysisRepository.findStaleSavedAnalyses(any(), any(), any()))
                    .willReturn(slice(List.of(analysis(a1), analysis(a2)), true),
                            slice(List.of(analysis(a3)), false));

            service.runRecheck();

            then(dispatcher).should().dispatch(a1);
            then(dispatcher).should().dispatch(a2);
            then(dispatcher).should().dispatch(a3);
            then(analysisRepository).should(times(2))
                    .findStaleSavedAnalyses(any(), any(), any());
        }

        @Test
        void 커서는_시작값에서_직전_배치의_마지막_id로_전진한다() {
            UUID a1 = UUID.randomUUID();
            UUID a2 = UUID.randomUUID();
            UUID a3 = UUID.randomUUID();
            given(analysisRepository.findStaleSavedAnalyses(any(), any(), any()))
                    .willReturn(slice(List.of(analysis(a1), analysis(a2)), true),
                            slice(List.of(analysis(a3)), false));

            service.runRecheck();

            ArgumentCaptor<UUID> cursor = ArgumentCaptor.forClass(UUID.class);
            then(analysisRepository).should(times(2))
                    .findStaleSavedAnalyses(any(), cursor.capture(), any());
            assertThat(cursor.getAllValues()).containsExactly(START_CURSOR, a2);
        }

        @Test
        void 대상이_없으면_위임하지_않는다() {
            given(analysisRepository.findStaleSavedAnalyses(any(), any(), any()))
                    .willReturn(slice(List.of(), false));

            service.runRecheck();

            then(dispatcher).shouldHaveNoInteractions();
        }

        @Test
        void 한_건이_실패해도_나머지를_계속_위임하고_예외를_던지지_않는다() {
            UUID a1 = UUID.randomUUID();
            UUID a2 = UUID.randomUUID();
            given(analysisRepository.findStaleSavedAnalyses(any(), any(), any()))
                    .willReturn(slice(List.of(analysis(a1), analysis(a2)), false));
            willThrow(new RuntimeException("boom")).given(dispatcher).dispatch(a1);

            assertThatCode(() -> service.runRecheck()).doesNotThrowAnyException();

            then(dispatcher).should().dispatch(a1);
            then(dispatcher).should().dispatch(a2);
        }
    }
}
