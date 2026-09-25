package com.mukplay.domain.game.model;

import lombok.Getter;

import java.time.Duration;
import java.time.Instant;

/**
 * Represents a single game round in an OX quiz match.
 */
@Getter
public class Round {

    public static final Duration DEFAULT_ROUND_DURATION = Duration.ofSeconds(10);

    private final int roundNumber;
    private final Long questionId;
    private final Answer answer;
    private final Instant startedAt;
    private final Instant endsAt;

    public Round(int roundNumber, Long questionId, Answer answer, Instant startedAt, Duration duration) {
        if (roundNumber <= 0) {
            throw new IllegalArgumentException("roundNumber는 1 이상이어야 합니다.");
        }
        if (questionId == null) {
            throw new IllegalArgumentException("questionId는 필수입니다.");
        }
        if (answer == null) {
            throw new IllegalArgumentException("answer(정답)는 필수입니다.");
        }
        if (startedAt == null) {
            throw new IllegalArgumentException("startedAt은 필수입니다.");
        }
        Duration validDuration = (duration == null || duration.isNegative() || duration.isZero())
                ? DEFAULT_ROUND_DURATION : duration;

        this.roundNumber = roundNumber;
        this.questionId = questionId;
        this.answer = answer;
        this.startedAt = startedAt;
        this.endsAt = startedAt.plus(validDuration);
    }

    public Round(int roundNumber, Long questionId, Answer answer) {
        this(roundNumber, questionId, answer, Instant.now(), DEFAULT_ROUND_DURATION);
    }

    public boolean isExpired(Instant currentTime) {
        return !currentTime.isBefore(this.endsAt);
    }

    public Duration getRemainingDuration(Instant currentTime) {
        if (isExpired(currentTime)) {
            return Duration.ZERO;
        }
        return Duration.between(currentTime, this.endsAt);
    }
}
