package com.mukplay.domain.game.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameSessionTest {

    @Test
    @DisplayName("GameSession은 순수 자바 객체로 깨끗하게 초기화되어야 한다")
    void testGameSessionInitialization() {
        GameSession session = new GameSession("room-101", 5);

        assertThat(session.getRoomId()).isEqualTo("room-101");
        assertThat(session.getState()).isEqualTo(GameSessionState.WAITING);
        assertThat(session.getCurrentRound()).isEqualTo(0);
        assertThat(session.getMaxRounds()).isEqualTo(5);
        assertThat(session.getStartedAt()).isNotNull();
        assertThat(session.getPlayers()).isEmpty();
    }

    @Test
    @DisplayName("허용된 상태 전이는 성공하고 비정상 상태 전이는 예외를 발생시켜야 한다")
    void testStateTransitions() {
        GameSession session = new GameSession("room-102", 3);

        session.transitionTo(GameSessionState.STARTING);
        assertThat(session.getState()).isEqualTo(GameSessionState.STARTING);

        session.transitionTo(GameSessionState.PLAYING);
        assertThat(session.getState()).isEqualTo(GameSessionState.PLAYING);

        session.transitionTo(GameSessionState.ROUND_END);
        assertThat(session.getState()).isEqualTo(GameSessionState.ROUND_END);

        session.transitionTo(GameSessionState.PLAYING);
        session.transitionTo(GameSessionState.FINISHED);
        assertThat(session.getState()).isEqualTo(GameSessionState.FINISHED);

        // FINISHED 이후에는 전이 불가
        assertThatThrownBy(() -> session.transitionTo(GameSessionState.PLAYING))
                .isInstanceOf(IllegalStateException.class);
    }
}
