package com.mukplay.domain.game.service;

import com.mukplay.domain.game.model.GameSession;
import com.mukplay.domain.game.model.GameSessionState;
import com.mukplay.domain.game.model.PlayerState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GameFinishTest {

    @Test
    @DisplayName("생존자가 1명 이하(최후의 생존자 발생)가 되면 게임 종료 조건이 충족된다")
    void finishesWhenLastSurvivorRemains() {
        GameFinishEvaluator evaluator = new GameFinishEvaluator();
        GameSession session = new GameSession("room-101", 10);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);

        PlayerState p1 = new PlayerState(1L, 20.0, 50.0);
        PlayerState p2 = new PlayerState(2L, 80.0, 50.0);
        PlayerState p3 = new PlayerState(3L, 40.0, 50.0);
        session.addPlayer(p1);
        session.addPlayer(p2);
        session.addPlayer(p3);

        assertThat(evaluator.isGameFinished(session)).isFalse();

        // 2명 탈락 -> 1명만 생존
        p2.eliminate();
        p3.eliminate();

        assertThat(session.getAlivePlayerCount()).isEqualTo(1);
        assertThat(evaluator.isGameFinished(session)).isTrue();

        evaluator.evaluateAndFinishIfMet(session);
        assertThat(session.getState()).isEqualTo(GameSessionState.FINISHED);
    }

    @Test
    @DisplayName("모든 플레이어가 탈락(생존자 0명)되어도 게임이 종료된다")
    void finishesWhenAllEliminated() {
        GameFinishEvaluator evaluator = new GameFinishEvaluator();
        GameSession session = new GameSession("room-102", 5);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);

        PlayerState p1 = new PlayerState(1L, 20.0, 50.0);
        session.addPlayer(p1);
        p1.eliminate();

        assertThat(session.getAlivePlayerCount()).isEqualTo(0);
        assertThat(evaluator.isGameFinished(session)).isTrue();

        evaluator.evaluateAndFinishIfMet(session);
        assertThat(session.getState()).isEqualTo(GameSessionState.FINISHED);
    }

    @Test
    @DisplayName("최대 라운드(maxRounds)에 도달하면 생존자가 여럿이어도 게임이 종료된다")
    void finishesWhenMaxRoundReached() {
        GameFinishEvaluator evaluator = new GameFinishEvaluator();
        GameSession session = new GameSession("room-103", 3);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);

        PlayerState p1 = new PlayerState(1L, 20.0, 50.0);
        PlayerState p2 = new PlayerState(2L, 80.0, 50.0);
        session.addPlayer(p1);
        session.addPlayer(p2);

        // 라운드 1, 2 진행
        session.nextRound();
        session.nextRound();
        assertThat(evaluator.isGameFinished(session)).isFalse();

        // 라운드 3 (maxRounds) 도달
        session.nextRound();
        assertThat(session.getCurrentRound()).isEqualTo(3);
        assertThat(evaluator.isGameFinished(session)).isTrue();

        evaluator.evaluateAndFinishIfMet(session);
        assertThat(session.getState()).isEqualTo(GameSessionState.FINISHED);
    }
}
