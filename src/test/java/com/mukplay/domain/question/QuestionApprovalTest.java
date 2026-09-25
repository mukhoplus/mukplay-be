package com.mukplay.domain.question;

import com.mukplay.domain.game.model.Answer;
import com.mukplay.domain.question.controller.QuestionController;
import com.mukplay.domain.question.dto.QuestionResponse;
import com.mukplay.domain.question.dto.QuestionStatusUpdateRequest;
import com.mukplay.domain.question.entity.Question;
import com.mukplay.domain.question.entity.QuestionDifficulty;
import com.mukplay.domain.question.entity.QuestionStatus;
import com.mukplay.domain.question.repository.QuestionRepository;
import com.mukplay.domain.question.service.QuestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QuestionApprovalTest {

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private QuestionService questionService;

    @Test
    @DisplayName("관리자가 문제를 승인(APPROVED)하면 문제 상태가 APPROVED로 변경된다")
    void adminCanApproveQuestion() {
        Question pendingQuestion = Question.builder()
                .content("지구는 둥글다?")
                .answer(Answer.O)
                .difficulty(QuestionDifficulty.EASY)
                .status(QuestionStatus.PENDING)
                .submitterId(1L)
                .build();

        given(questionRepository.findById(10L)).willReturn(Optional.of(pendingQuestion));

        QuestionResponse response = questionService.updateQuestionStatus(10L, QuestionStatus.APPROVED);

        assertThat(response.status()).isEqualTo(QuestionStatus.APPROVED);
        assertThat(pendingQuestion.getStatus()).isEqualTo(QuestionStatus.APPROVED);
    }

    @Test
    @DisplayName("관리자가 문제를 반려(REJECTED)하면 문제 상태가 REJECTED로 변경된다")
    void adminCanRejectQuestion() {
        Question pendingQuestion = Question.builder()
                .content("달은 치즈로 만들어졌다?")
                .answer(Answer.X)
                .difficulty(QuestionDifficulty.HARD)
                .status(QuestionStatus.PENDING)
                .submitterId(2L)
                .build();

        given(questionRepository.findById(20L)).willReturn(Optional.of(pendingQuestion));

        QuestionResponse response = questionService.updateQuestionStatus(20L, QuestionStatus.REJECTED);

        assertThat(response.status()).isEqualTo(QuestionStatus.REJECTED);
        assertThat(pendingQuestion.getStatus()).isEqualTo(QuestionStatus.REJECTED);
    }
}
