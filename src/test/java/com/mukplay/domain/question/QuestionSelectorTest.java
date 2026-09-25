package com.mukplay.domain.question;

import com.mukplay.common.exception.BusinessException;
import com.mukplay.domain.game.model.Answer;
import com.mukplay.domain.question.entity.Question;
import com.mukplay.domain.question.entity.QuestionDifficulty;
import com.mukplay.domain.question.entity.QuestionStatus;
import com.mukplay.domain.question.repository.QuestionRepository;
import com.mukplay.domain.question.service.QuestionPoolSelector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QuestionSelectorTest {

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private QuestionPoolSelector poolSelector;

    @Test
    @DisplayName("게임 진행을 위해 승인(APPROVED)된 문제 풀에서만 요청한 개수만큼 선택된다")
    void selectOnlyApprovedQuestions() {
        Question q1 = Question.builder().content("문제 1").answer(Answer.O).status(QuestionStatus.APPROVED).submitterId(1L).build();
        Question q2 = Question.builder().content("문제 2").answer(Answer.X).status(QuestionStatus.APPROVED).submitterId(2L).build();
        Question q3 = Question.builder().content("문제 3").answer(Answer.O).status(QuestionStatus.APPROVED).submitterId(3L).build();

        given(questionRepository.findByStatus(QuestionStatus.APPROVED)).willReturn(List.of(q1, q2, q3));

        List<Question> selected = poolSelector.selectQuestionsForGame(2);

        assertThat(selected).hasSize(2);
        assertThat(selected).allMatch(q -> q.getStatus() == QuestionStatus.APPROVED);
    }

    @Test
    @DisplayName("승인된 문제 수가 요청 개수보다 부족하면 예외를 발생시킨다")
    void throwExceptionWhenApprovedQuestionsNotEnough() {
        Question q1 = Question.builder().content("문제 1").answer(Answer.O).status(QuestionStatus.APPROVED).submitterId(1L).build();

        given(questionRepository.findByStatus(QuestionStatus.APPROVED)).willReturn(List.of(q1));

        assertThatThrownBy(() -> poolSelector.selectQuestionsForGame(3))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("승인된 문제가 부족합니다");
    }
}
