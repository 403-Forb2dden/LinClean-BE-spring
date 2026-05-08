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
        Terms terms = Terms.builder()
                .type(TermsType.PRIVACY_POLICY)
                .title("개인정보 처리방침")
                .content("제1조 ...")
                .contentFormat(ContentFormat.MARKDOWN)
                .effectiveAt(Instant.parse("2026-05-01T00:00:00Z"))
                .build();
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
