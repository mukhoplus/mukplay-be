package com.mukplay.domain.question.dto;

import com.mukplay.domain.question.entity.QuestionStatus;
import jakarta.validation.constraints.NotNull;

public record QuestionStatusUpdateRequest(
        @NotNull(message = "변경할 상태는 필수입니다.")
        QuestionStatus status
) {}
