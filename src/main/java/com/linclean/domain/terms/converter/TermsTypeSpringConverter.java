package com.linclean.domain.terms.converter;

import com.linclean.domain.terms.entity.TermsType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TermsTypeSpringConverter implements Converter<String, TermsType> {

    @Override
    public TermsType convert(String source) {
        return TermsType.from(source);
    }
}
