package com.mukplay.domain.question.entity;

import com.mukplay.common.entity.BaseTimeEntity;
import com.mukplay.domain.game.model.Answer;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Answer answer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionDifficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionStatus status;

    @Column(name = "submitter_id", nullable = false)
    private Long submitterId;

    @Builder
    public Question(String content, Answer answer, QuestionDifficulty difficulty, QuestionStatus status, Long submitterId) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("문제 내용은 필수입니다.");
        }
        if (answer == null) {
            throw new IllegalArgumentException("정답은 필수입니다.");
        }
        if (submitterId == null) {
            throw new IllegalArgumentException("제출자 ID는 필수입니다.");
        }

        this.content = content;
        this.answer = answer;
        this.difficulty = difficulty != null ? difficulty : QuestionDifficulty.NORMAL;
        this.status = status != null ? status : QuestionStatus.PENDING;
        this.submitterId = submitterId;
    }

    public void approve() {
        this.status = QuestionStatus.APPROVED;
    }

    public void reject() {
        this.status = QuestionStatus.REJECTED;
    }
}
