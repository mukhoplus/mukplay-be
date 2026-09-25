package com.mukplay.domain.game.validator;

import com.mukplay.domain.game.model.Direction;
import com.mukplay.domain.game.model.GameSession;
import com.mukplay.domain.game.model.GameSessionState;
import com.mukplay.domain.game.model.PlayerState;
import com.mukplay.domain.game.service.MovementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MovementValidatorTest {

    @Test
    @DisplayName("게임 상태가 PLAYING이 아닐 때 이동 요청은 거부되어야 한다")
    void testNonPlayingStateMovementRejected() {
        GameSession session = new GameSession("room-val-1", 3);
        PlayerState player = new PlayerState(1L, 50.0, 50.0);

        // WAITING 상태
        assertThatThrownBy(() -> MovementService.applySessionMovement(session, player, Direction.UP))
                .isInstanceOf(IllegalStateException.class);

        // STARTING 상태
        session.transitionTo(GameSessionState.STARTING);
        assertThatThrownBy(() -> MovementService.applySessionMovement(session, player, Direction.UP))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("탈락한 플레이어의 이동 요청은 거부되어야 한다")
    void testDeadPlayerMovementRejected() {
        GameSession session = new GameSession("room-val-2", 3);
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);

        PlayerState player = new PlayerState(2L, 50.0, 50.0);
        player.eliminate();

        assertThatThrownBy(() -> MovementService.applySessionMovement(session, player, Direction.UP))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("맵 경계선을 벗어나는 이동은 0.0과 100.0 범위로 Clamping 되어야 한다")
    void testBoundaryClamping() {
        // Upper-left corner
        PlayerState player = new PlayerState(3L, 1.0, 1.0);

        // Move LEFT by 5.0 -> Clamped to 0.0
        MovementService.applyMovement(player, Direction.LEFT, 5.0);
        assertThat(player.getX()).isEqualTo(0.0);

        // Move UP by 5.0 -> Clamped to 0.0
        MovementService.applyMovement(player, Direction.UP, 5.0);
        assertThat(player.getY()).isEqualTo(0.0);

        // Bottom-right corner
        player.moveTo(99.0, 99.0);

        // Move RIGHT by 5.0 -> Clamped to 100.0
        MovementService.applyMovement(player, Direction.RIGHT, 5.0);
        assertThat(player.getX()).isEqualTo(100.0);

        // Move DOWN by 5.0 -> Clamped to 100.0
        MovementService.applyMovement(player, Direction.DOWN, 5.0);
        assertThat(player.getY()).isEqualTo(100.0);
    }
}
