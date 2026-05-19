package com.linclean.domain.link.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SavedLinkTitleUpdateRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 500, message = "제목은 500자 이하여야 합니다.")
        String title
) {}
