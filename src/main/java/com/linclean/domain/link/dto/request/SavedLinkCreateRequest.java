package com.linclean.domain.link.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SavedLinkCreateRequest(
        @NotNull(message = "분석 ID는 필수입니다.")
        UUID analysisId,
        Long categoryId,
        String title,
        String description
) {}
