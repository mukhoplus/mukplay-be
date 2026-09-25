package com.mukplay.domain.game.service;

import com.mukplay.domain.game.model.Answer;
import com.mukplay.domain.game.model.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class RoundTimerTest {

    @Test
    @DisplayName("서버 시계 기준 시작 후 10초 이전에는 라운드가 종료되지 않는다")
    void roundNotEndedBefore10Seconds() {
        Instant startTime = Instant.parse("2026-09-25T12:00:00Z");
        Round round = new Round(1, 10L, Answer.O, startTime, Duration.ofSeconds(10));

        // 서버 시간이 7초 경과 시점
        Clock clockAt7Seconds = Clock.fixed(startTime.plusSeconds(7), ZoneOffset.UTC);
        RoundTimerService timerService = new RoundTimerService(clockAt7Seconds);

        assertThat(timerService.isRoundEnded(round)).isFalse();
        assertThat(timerService.getRemainingSeconds(round)).isEqualTo(3);
    }

    @Test
    @DisplayName("서버 시계 기준 10초가 경과하면 라운드 종료로 판정한다")
    void roundEndedAfter10Seconds() {
        Instant startTime = Instant.parse("2026-09-25T12:00:00Z");
        Round round = new Round(1, 10L, Answer.O, startTime, Duration.ofSeconds(10));

        // 서버 시간이 정확히 10초 경과 시점
        Clock clockAt10Seconds = Clock.fixed(startTime.plusSeconds(10), ZoneOffset.UTC);
        RoundTimerService timerService = new RoundTimerService(clockAt10Seconds);

        assertThat(timerService.isRoundEnded(round)).isTrue();
        assertThat(timerService.getRemainingSeconds(round)).isEqualTo(0);

        // 서버 시간이 15초 경과 시점
        Clock clockAt15Seconds = Clock.fixed(startTime.plusSeconds(15), ZoneOffset.UTC);
        RoundTimerService timerServiceLater = new RoundTimerService(clockAt15Seconds);

        assertThat(timerServiceLater.isRoundEnded(round)).isTrue();
        assertThat(timerServiceLater.getRemainingSeconds(round)).isEqualTo(0);
    }
}
