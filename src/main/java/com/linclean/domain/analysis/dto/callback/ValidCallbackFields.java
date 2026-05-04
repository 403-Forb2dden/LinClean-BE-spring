package com.linclean.domain.analysis.dto.callback;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CallbackFieldsValidator.class)
public @interface ValidCallbackFields {
    String message() default "SUCCEEDED 콜백에는 finalUrl, verdict, score, reasons, stages, summary가 필요합니다. FAILED 콜백에는 error가 필요합니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
