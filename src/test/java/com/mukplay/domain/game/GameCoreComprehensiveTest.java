package com.mukplay.domain.game;

import com.mukplay.domain.game.model.*;
import com.mukplay.domain.game.service.EliminationService;
import com.mukplay.domain.game.service.GameFinishEvaluator;
import com.mukplay.domain.game.service.MovementService;
import com.mukplay.domain.game.service.RoundTimerService;
import com.mukplay.domain.game.validator.MovementValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameCoreComprehensiveTest {

    private MovementService movementService;
    private EliminationService eliminationService;
    private GameFinishEvaluator finishEvaluator;

    @BeforeEach
    void setUp() {
        movementService = new MovementService();
        eliminationService = new EliminationService();
        finishEvaluator = new GameFinishEvaluator();
    }

    @Test
    @DisplayName("Case 1: normal movement - 정상 이동")
    void case1_normalMovement() {
        GameSession session = new GameSession("room-c1", 5);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);

        PlayerState player = new PlayerState(1L, 50.0, 50.0);
        session.addPlayer(player);

        movementService.move(session, player, Direction.UP);
        assertThat(player.getY()).isEqualTo(50.0 - MovementService.STEP_SIZE);

        movementService.move(session, player, Direction.RIGHT);
        assertThat(player.getX()).isEqualTo(50.0 + MovementService.STEP_SIZE);
    }

    @Test
    @DisplayName("Case 2: boundary violation clamping - 경계 밖 이동 시 0.0~100.0 클램핑")
    void case2_boundaryClamping() {
        GameSession session = new GameSession("room-c2", 5);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);

        PlayerState player = new PlayerState(1L, 0.5, 99.5);
        session.addPlayer(player);

        // 왼쪽으로 이동하여 0 미만 시도 -> 0.0 클램핑
        movementService.move(session, player, Direction.LEFT);
        assertThat(player.getX()).isEqualTo(MovementValidator.MIN_X);

        // 아래쪽으로 이동하여 100 초과 시도 -> 100.0 클램핑
        movementService.move(session, player, Direction.DOWN);
        assertThat(player.getY()).isEqualTo(MovementValidator.MAX_Y);
    }

    @Test
    @DisplayName("Case 3: dead player move rejection - 탈락한 플레이어 이동 거부")
    void case3_deadPlayerMoveRejection() {
        GameSession session = new GameSession("room-c3", 5);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);

        PlayerState player = new PlayerState(1L, 50.0, 50.0);
        player.eliminate();
        session.addPlayer(player);

        assertThatThrownBy(() -> movementService.move(session, player, Direction.UP))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("탈락");
    }

    @Test
    @DisplayName("Case 4: invalid state transition - 비정상 상태 전이 예외 발생")
    void case4_invalidStateTransition() {
        GameSession session = new GameSession("room-c4", 5);

        // WAITING -> 바로 FINISHED 전이는 불가
        assertThatThrownBy(() -> session.transitionTo(GameSessionState.FINISHED))
                .isInstanceOf(IllegalStateException.class);

        // WAITING -> STARTING -> PLAYING -> FINISHED
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);
        session.transitionTo(GameSessionState.FINISHED);

        // FINISHED 이후 상태 변경 불가
        assertThatThrownBy(() -> session.transitionTo(GameSessionState.WAITING))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Case 5: round start & end - 라운드 시작 및 타이머 만료 판정")
    void case5_roundStartAndEnd() {
        Instant startTime = Instant.parse("2026-09-25T12:00:00Z");
        Round round = new Round(1, 100L, Answer.O, startTime, Duration.ofSeconds(10));

        Clock clockBefore10s = Clock.fixed(startTime.plusSeconds(9), ZoneOffset.UTC);
        RoundTimerService timerBefore = new RoundTimerService(clockBefore10s);
        assertThat(timerBefore.isRoundEnded(round)).isFalse();

        Clock clockAfter10s = Clock.fixed(startTime.plusSeconds(10), ZoneOffset.UTC);
        RoundTimerService timerAfter = new RoundTimerService(clockAfter10s);
        assertThat(timerAfter.isRoundEnded(round)).isTrue();
    }

    @Test
    @DisplayName("Case 6: correct zone survival - 정답 영역 위치 플레이어 생존")
    void case6_correctZoneSurvival() {
        GameSession session = new GameSession("room-c6", 5);
        PlayerState player = new PlayerState(1L, 20.0, 50.0); // O zone
        session.addPlayer(player);

        Round round = new Round(1, 101L, Answer.O, Instant.now(), Duration.ofSeconds(10));
        List<Long> eliminated = eliminationService.processRoundElimination(session, round);

        assertThat(eliminated).isEmpty();
        assertThat(player.isAlive()).isTrue();
    }

    @Test
    @DisplayName("Case 7: wrong zone / neutral zone elimination - 오답 및 중립 구역 탈락")
    void case7_wrongAndNeutralZoneElimination() {
        GameSession session = new GameSession("room-c7", 5);
        PlayerState wrongPlayer = new PlayerState(1L, 75.0, 50.0); // X zone
        PlayerState neutralPlayer = new PlayerState(2L, 50.0, 50.0); // Neutral zone (45~55)
        session.addPlayer(wrongPlayer);
        session.addPlayer(neutralPlayer);

        Round round = new Round(1, 102L, Answer.O, Instant.now(), Duration.ofSeconds(10));
        List<Long> eliminated = eliminationService.processRoundElimination(session, round);

        assertThat(eliminated).containsExactlyInAnyOrder(1L, 2L);
        assertThat(wrongPlayer.isAlive()).isFalse();
        assertThat(neutralPlayer.isAlive()).isFalse();
    }

    @Test
    @DisplayName("Case 8: last survivor victory - 최후 1인 생존 시 게임 자동 종료 충족")
    void case8_lastSurvivorVictory() {
        GameSession session = new GameSession("room-c8", 5);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);

        PlayerState p1 = new PlayerState(1L, 30.0, 50.0);
        PlayerState p2 = new PlayerState(2L, 70.0, 50.0);
        session.addPlayer(p1);
        session.addPlayer(p2);

        Round round = new Round(1, 103L, Answer.O, Instant.now(), Duration.ofSeconds(10));
        eliminationService.processRoundElimination(session, round);

        assertThat(session.getAlivePlayerCount()).isEqualTo(1);
        assertThat(session.getSurvivors().getFirst().getUserId()).isEqualTo(1L);

        finishEvaluator.evaluateAndFinishIfMet(session);
        assertThat(session.getState()).isEqualTo(GameSessionState.FINISHED);
    }

    @Test
    @DisplayName("Case 9: max round completion - 최대 라운드 도달 시 게임 종료")
    void case9_maxRoundCompletion() {
        GameSession session = new GameSession("room-c9", 2);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);

        session.addPlayer(new PlayerState(1L, 30.0, 50.0));
        session.addPlayer(new PlayerState(2L, 30.0, 50.0));

        session.nextRound(); // round 1
        assertThat(finishEvaluator.isGameFinished(session)).isFalse();

        session.nextRound(); // round 2 (max round reached)
        assertThat(finishEvaluator.isGameFinished(session)).isTrue();

        finishEvaluator.evaluateAndFinishIfMet(session);
        assertThat(session.getState()).isEqualTo(GameSessionState.FINISHED);
    }

    @Test
    @DisplayName("Case 10: duplicate finish idempotency - 이미 FINISHED 상태에서의 중복 호출 멱등성 보장")
    void case10_duplicateFinishIdempotency() {
        GameSession session = new GameSession("room-c10", 3);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);
        session.transitionTo(GameSessionState.FINISHED);

        // 중복 finish 평가 시 예외 없이 상태 유지
        finishEvaluator.evaluateAndFinishIfMet(session);
        assertThat(session.getState()).isEqualTo(GameSessionState.FINISHED);
    }
}
