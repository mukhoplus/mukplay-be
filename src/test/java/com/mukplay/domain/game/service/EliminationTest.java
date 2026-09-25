package com.mukplay.domain.game.service;

import com.mukplay.domain.game.model.Answer;
import com.mukplay.domain.game.model.GameSession;
import com.mukplay.domain.game.model.PlayerState;
import com.mukplay.domain.game.model.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EliminationTest {

    @Test
    @DisplayName("라운드 정답이 O일 때, X 구역이나 중립 구역에 있는 플레이어는 탈락하고 O 구역 플레이어는 생존한다")
    void eliminateIncorrectPlayersWhenAnswerIsO() {
        EliminationService eliminationService = new EliminationService();
        GameSession session = new GameSession("room-1", 5);

        // Player 1: O 영역 (x = 30.0) -> 생존
        PlayerState p1 = new PlayerState(1L, 30.0, 50.0);
        // Player 2: X 영역 (x = 70.0) -> 탈락
        PlayerState p2 = new PlayerState(2L, 70.0, 50.0);
        // Player 3: 중립 영역 (x = 50.0) -> 탈락
        PlayerState p3 = new PlayerState(3L, 50.0, 50.0);
        // Player 4: 이미 탈락한 플레이어
        PlayerState p4 = new PlayerState(4L, 30.0, 50.0);
        p4.eliminate();

        session.addPlayer(p1);
        session.addPlayer(p2);
        session.addPlayer(p3);
        session.addPlayer(p4);

        Round round = new Round(1, 10L, Answer.O, Instant.now(), Duration.ofSeconds(10));

        List<Long> eliminatedIds = eliminationService.processRoundElimination(session, round);

        // 검증: 오답 및 중립 구역에 있던 2L, 3L 탈락
        assertThat(eliminatedIds).containsExactlyInAnyOrder(2L, 3L);
        assertThat(p1.isAlive()).isTrue();
        assertThat(p2.isAlive()).isFalse();
        assertThat(p3.isAlive()).isFalse();
        assertThat(p4.isAlive()).isFalse();
    }

    @Test
    @DisplayName("라운드 정답이 X일 때, X 구역 플레이어만 생존한다")
    void eliminateIncorrectPlayersWhenAnswerIsX() {
        EliminationService eliminationService = new EliminationService();
        GameSession session = new GameSession("room-2", 3);

        PlayerState p1 = new PlayerState(1L, 20.0, 50.0); // O zone -> 탈락
        PlayerState p2 = new PlayerState(2L, 80.0, 50.0); // X zone -> 생존

        session.addPlayer(p1);
        session.addPlayer(p2);

        Round round = new Round(1, 20L, Answer.X, Instant.now(), Duration.ofSeconds(10));

        List<Long> eliminatedIds = eliminationService.processRoundElimination(session, round);

        assertThat(eliminatedIds).containsExactly(1L);
        assertThat(p1.isAlive()).isFalse();
        assertThat(p2.isAlive()).isTrue();
    }
}
