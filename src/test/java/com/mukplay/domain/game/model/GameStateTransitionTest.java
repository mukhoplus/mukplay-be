package com.mukplay.domain.game.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameStateTransitionTest {

    @Test
    @DisplayName("정상적인 상태 전이 사이클이 성공해야 한다: WAITING -> STARTING -> PLAYING -> ROUND_END -> PLAYING -> FINISHED")
    void testValidStateCycle() {
        GameSession session = new GameSession("room-cycle-1", 5);

        session.transitionTo(GameSessionState.STARTING);
        assertThat(session.getState()).isEqualTo(GameSessionState.STARTING);

        session.transitionTo(GameSessionState.PLAYING);
        assertThat(session.getState()).isEqualTo(GameSessionState.PLAYING);

        // Round 1 end
        session.transitionTo(GameSessionState.ROUND_END);
        assertThat(session.getState()).isEqualTo(GameSessionState.ROUND_END);

        // Round 2 start
        session.transitionTo(GameSessionState.PLAYING);
        assertThat(session.getState()).isEqualTo(GameSessionState.PLAYING);

        // Game finish
        session.transitionTo(GameSessionState.FINISHED);
        assertThat(session.getState()).isEqualTo(GameSessionState.FINISHED);
    }

    @Test
    @DisplayName("WAITING 상태에서 PLAYING으로 직행하는 비정상 전이는 거부되어야 한다")
    void testDirectPlayingFromWaitingRejected() {
        GameSession session = new GameSession("room-invalid-1", 3);

        assertThatThrownBy(() -> session.transitionTo(GameSessionState.PLAYING))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("WAITING 상태에서 FINISHED로 직행하는 비정상 전이는 거부되어야 한다")
    void testDirectFinishedFromWaitingRejected() {
        GameSession session = new GameSession("room-invalid-2", 3);

        assertThatThrownBy(() -> session.transitionTo(GameSessionState.FINISHED))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = GameSessionState.class)
    @DisplayName("FINISHED 상태에서는 어떤 상태로도 다시 전이될 수 없다")
    void testFinishedCannotTransition(GameSessionState targetState) {
        GameSession session = new GameSession("room-finished-test", 3);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);
        session.transitionTo(GameSessionState.FINISHED);

        assertThatThrownBy(() -> session.transitionTo(targetState))
                .isInstanceOf(IllegalStateException.class);
    }
}
