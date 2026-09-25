package com.mukplay.domain.question.service;

import com.mukplay.domain.question.dto.QuestionResponse;
import com.mukplay.domain.question.dto.QuestionSubmitRequest;
import com.mukplay.domain.question.entity.Question;
import com.mukplay.domain.question.entity.QuestionStatus;
import com.mukplay.domain.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    @Transactional
    public QuestionResponse submitQuestion(Long submitterId, QuestionSubmitRequest request) {
        Question question = Question.builder()
                .content(request.content())
                .answer(request.answer())
                .difficulty(request.difficulty())
                .status(QuestionStatus.PENDING)
                .submitterId(submitterId)
                .build();

        Question saved = questionRepository.save(question);
        return QuestionResponse.from(saved);
    }

    @Transactional
    public QuestionResponse updateQuestionStatus(Long questionId, QuestionStatus newStatus) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new com.mukplay.common.exception.BusinessException(
                        com.mukplay.common.exception.ErrorCode.ENTITY_NOT_FOUND, "문제를 찾을 수 없습니다. id=" + questionId));

        if (newStatus == QuestionStatus.APPROVED) {
            question.approve();
        } else if (newStatus == QuestionStatus.REJECTED) {
            question.reject();
        }

        return QuestionResponse.from(question);
    }
}
