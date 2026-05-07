package com.linclean.domain.analysis.dto.callback;

import com.linclean.domain.analysis.entity.AnalysisStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CallbackFieldsValidator
        implements ConstraintValidator<ValidCallbackFields, AnalysisResultCallback> {

    @Override
    public boolean isValid(AnalysisResultCallback dto, ConstraintValidatorContext ctx) {
        if (dto.status() == AnalysisStatus.SUCCEEDED) {
            return dto.finalUrl() != null
                    && dto.verdict() != null
                    && dto.score() != null
                    && dto.stages() != null
                    && dto.reasons() != null
                    && dto.summary() != null;
        }
        if (dto.status() == AnalysisStatus.FAILED) {
            return dto.error() != null;
        }
        return true;
    }
}