package com.linclean.domain.link.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class SavedLinkRecheckSchedulerTest {

    @Mock SavedLinkRecheckService recheckService;
    @InjectMocks SavedLinkRecheckScheduler scheduler;

    @Nested
    class recheckStaleSavedLinks {

        @Test
        void 스케줄_실행시_재검사_서비스를_호출한다() {
            scheduler.recheckStaleSavedLinks();

            then(recheckService).should().runRecheck();
        }

        @Test
        void 재검사_서비스가_예외를_던져도_스케줄러가_삼킨다() {
            willThrow(new RuntimeException("boom")).given(recheckService).runRecheck();

            assertThatCode(() -> scheduler.recheckStaleSavedLinks()).doesNotThrowAnyException();
            then(recheckService).should().runRecheck();
        }
    }
}
