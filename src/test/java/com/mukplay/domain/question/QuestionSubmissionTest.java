package com.mukplay.domain.question;

import com.mukplay.domain.game.model.Answer;
import com.mukplay.domain.question.dto.QuestionResponse;
import com.mukplay.domain.question.dto.QuestionSubmitRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QuestionSubmissionTest {

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private QuestionService questionService;

    @Test
    @DisplayName("문제 제출 시 초기 상태는 PENDING이며 올바른 정보가 등록된다")
    void submitQuestionCreatesPendingStatus() {
        QuestionSubmitRequest request = new QuestionSubmitRequest(
                "사과는 빨간색이다?",
                Answer.O,
                QuestionDifficulty.EASY
        );

        Question mockSaved = Question.builder()
                .content(request.content())
                .answer(request.answer())
                .difficulty(request.difficulty())
                .status(QuestionStatus.PENDING)
                .submitterId(99L)
                .build();

        given(questionRepository.save(any(Question.class))).willReturn(mockSaved);

        QuestionResponse response = questionService.submitQuestion(99L, request);

        assertThat(response.content()).isEqualTo("사과는 빨간색이다?");
        assertThat(response.answer()).isEqualTo(Answer.O);
        assertThat(response.difficulty()).isEqualTo(QuestionDifficulty.EASY);
        assertThat(response.status()).isEqualTo(QuestionStatus.PENDING);
        assertThat(response.submitterId()).isEqualTo(99L);
    }
}
