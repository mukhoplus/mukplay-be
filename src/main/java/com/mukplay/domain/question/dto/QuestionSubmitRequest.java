package com.mukplay.domain.question.dto;

import com.mukplay.domain.game.model.Answer;
import com.mukplay.domain.question.entity.QuestionDifficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuestionSubmitRequest(
        @NotBlank(message = "문제 내용은 필수입니다.")
        @Size(max = 500, message = "문제 내용은 500자 이하이어야 합니다.")
        String content,

        @NotNull(message = "정답(O 또는 X)은 필수입니다.")
        Answer answer,

        QuestionDifficulty difficulty
) {}
