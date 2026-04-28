package com.linclean.domain.analysis.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnalysisRequest(
        @NotBlank(message = "URL은 필수입니다.")
        @Size(max = 2048, message = "URL은 2048자를 초과할 수 없습니다.")
        String url
) {}
