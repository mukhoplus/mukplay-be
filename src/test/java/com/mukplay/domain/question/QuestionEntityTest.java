package com.mukplay.domain.question;

import com.mukplay.domain.game.model.Answer;
import com.mukplay.domain.question.entity.Question;
import com.mukplay.domain.question.entity.QuestionDifficulty;
import com.mukplay.domain.question.entity.QuestionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestionEntityTest {

    @Test
    @DisplayName("Question 엔티티 생성 시 기본 상태는 PENDING, 기본 난이도는 NORMAL이다")
    void createQuestionDefaultValues() {
        Question question = Question.builder()
                .content("지구는 둥글다?")
                .answer(Answer.O)
                .submitterId(10L)
                .build();

        assertThat(question.getContent()).isEqualTo("지구는 둥글다?");
        assertThat(question.getAnswer()).isEqualTo(Answer.O);
        assertThat(question.getDifficulty()).isEqualTo(QuestionDifficulty.NORMAL);
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.PENDING);
        assertThat(question.getSubmitterId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Question 엔티티 상태를 APPROVED 및 REJECTED 로 변경할 수 있다")
    void changeStatus() {
        Question question = Question.builder()
                .content("토마토는 과일이다?")
                .answer(Answer.X)
                .difficulty(QuestionDifficulty.EASY)
                .submitterId(20L)
                .build();

        question.approve();
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.APPROVED);

        question.reject();
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.REJECTED);
    }

    @Test
    @DisplayName("필수 항목(content, answer, submitterId)이 누락되면 예외가 발생한다")
    void validateMandatoryFields() {
        assertThatThrownBy(() -> Question.builder()
                .content(null)
                .answer(Answer.O)
                .submitterId(1L)
                .build())
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Question.builder()
                .content("질문")
                .answer(null)
                .submitterId(1L)
                .build())
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Question.builder()
                .content("질문")
                .answer(Answer.O)
                .submitterId(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
