package com.mukplay.domain.question.dto;

import com.mukplay.domain.game.model.Answer;
import com.mukplay.domain.question.entity.Question;
import com.mukplay.domain.question.entity.QuestionDifficulty;
import com.mukplay.domain.question.entity.QuestionStatus;

import java.time.LocalDateTime;

public record QuestionResponse(
        Long id,
        String content,
        Answer answer,
        QuestionDifficulty difficulty,
        QuestionStatus status,
        Long submitterId,
        LocalDateTime createdAt
) {
    public static QuestionResponse from(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getContent(),
                question.getAnswer(),
                question.getDifficulty(),
                question.getStatus(),
                question.getSubmitterId(),
                question.getCreatedAt()
        );
    }
}
