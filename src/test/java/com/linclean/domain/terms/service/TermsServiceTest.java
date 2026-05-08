package com.linclean.domain.terms.service;

import com.linclean.domain.terms.dto.TermsResponse;
import com.linclean.domain.terms.entity.ContentFormat;
import com.linclean.domain.terms.entity.Terms;
import com.linclean.domain.terms.entity.TermsType;
import com.linclean.domain.terms.exception.TermsException;
import com.linclean.domain.terms.repository.TermsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class TermsServiceTest {

    @Mock TermsRepository termsRepository;
    @InjectMocks TermsService termsService;

    @Test
    void getTerms_found_returnsResponse() {
        Terms terms = mock(Terms.class);
        given(terms.getType()).willReturn(TermsType.PRIVACY_POLICY);
        given(terms.getTitle()).willReturn("개인정보 처리방침");
        given(terms.getContent()).willReturn("제1조 ...");
        given(terms.getContentFormat()).willReturn(ContentFormat.MARKDOWN);
        given(terms.getEffectiveAt()).willReturn(Instant.parse("2026-05-01T00:00:00Z"));
        given(terms.getUpdatedAt()).willReturn(Instant.parse("2026-05-01T00:00:00Z"));
        given(termsRepository.findByType(TermsType.PRIVACY_POLICY)).willReturn(Optional.of(terms));

        TermsResponse response = termsService.getTerms(TermsType.PRIVACY_POLICY);

        assertThat(response.type()).isEqualTo(TermsType.PRIVACY_POLICY);
        assertThat(response.title()).isEqualTo("개인정보 처리방침");
        assertThat(response.contentFormat()).isEqualTo(ContentFormat.MARKDOWN);
    }

    @Test
    void getTerms_notFound_throwsTermsException() {
        given(termsRepository.findByType(TermsType.SERVICE_GUIDE)).willReturn(Optional.empty());

        assertThatThrownBy(() -> termsService.getTerms(TermsType.SERVICE_GUIDE))
                .isInstanceOf(TermsException.class);
    }
}
