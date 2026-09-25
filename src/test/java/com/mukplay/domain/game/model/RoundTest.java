package com.mukplay.domain.game.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoundTest {

    @Test
    @DisplayName("기본 생성자로 라운드 생성 시 10초 후에 만료된다")
    void defaultRoundCreation() {
        Instant now = Instant.parse("2026-09-25T12:00:00Z");
        Round round = new Round(1, 100L, Answer.O, now, Round.DEFAULT_ROUND_DURATION);

        assertThat(round.getRoundNumber()).isEqualTo(1);
        assertThat(round.getQuestionId()).isEqualTo(100L);
        assertThat(round.getAnswer()).isEqualTo(Answer.O);
        assertThat(round.getStartedAt()).isEqualTo(now);
        assertThat(round.getEndsAt()).isEqualTo(now.plusSeconds(10));
    }

    @Test
    @DisplayName("라운드 만료 여부 및 잔여 시간을 정확히 계산한다")
    void expirationAndRemainingDuration() {
        Instant startTime = Instant.parse("2026-09-25T12:00:00Z");
        Round round = new Round(1, 100L, Answer.X, startTime, Duration.ofSeconds(10));

        // 5초 경과
        Instant midTime = startTime.plusSeconds(5);
        assertThat(round.isExpired(midTime)).isFalse();
        assertThat(round.getRemainingDuration(midTime)).isEqualTo(Duration.ofSeconds(5));

        // 10초 경과 (정확히 만료 시점)
        Instant exactEndTime = startTime.plusSeconds(10);
        assertThat(round.isExpired(exactEndTime)).isTrue();
        assertThat(round.getRemainingDuration(exactEndTime)).isEqualTo(Duration.ZERO);

        // 12초 경과 (만료 후)
        Instant afterEndTime = startTime.plusSeconds(12);
        assertThat(round.isExpired(afterEndTime)).isTrue();
        assertThat(round.getRemainingDuration(afterEndTime)).isEqualTo(Duration.ZERO);
    }

    @Test
    @DisplayName("잘못된 인자값 전달 시 예외를 던진다")
    void validationErrors() {
        Instant now = Instant.now();

        assertThatThrownBy(() -> new Round(0, 1L, Answer.O, now, Duration.ofSeconds(10)))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Round(1, null, Answer.O, now, Duration.ofSeconds(10)))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Round(1, 1L, null, now, Duration.ofSeconds(10)))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Round(1, 1L, Answer.O, null, Duration.ofSeconds(10)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
