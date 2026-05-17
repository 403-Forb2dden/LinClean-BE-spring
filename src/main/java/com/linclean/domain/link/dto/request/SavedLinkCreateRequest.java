package com.linclean.domain.link.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SavedLinkCreateRequest(
        @NotNull(message = "분석 ID는 필수입니다.")
        UUID analysisId,
        Long categoryId,
        @Size(max = 500, message = "제목은 500자 이하여야 합니다.")
        String title,
        @Size(max = 5000, message = "설명은 5000자 이하여야 합니다.")
        String description
) {}
