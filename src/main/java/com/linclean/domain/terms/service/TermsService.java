package com.linclean.domain.terms.service;

import com.linclean.domain.terms.dto.TermsResponse;
import com.linclean.domain.terms.entity.TermsType;
import com.linclean.domain.terms.exception.TermsException;
import com.linclean.domain.terms.repository.TermsRepository;
import com.linclean.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TermsService {

    private final TermsRepository termsRepository;

    @Transactional(readOnly = true)
    public TermsResponse getTerms(TermsType type) {
        return termsRepository.findByType(type)
                .map(TermsResponse::from)
                .orElseThrow(() -> new TermsException(ErrorCode.TERMS_NOT_FOUND));
    }
}
