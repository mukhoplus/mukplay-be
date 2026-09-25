package com.mukplay.domain.game.service;

import com.mukplay.domain.game.model.Direction;
import com.mukplay.domain.game.model.PlayerState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthoritativeMovementTest {

    @Test
    @DisplayName("서버는 클라이언트 좌표를 받지 않고 오직 Direction으로만 새 위치를 계산한다")
    void testAuthoritativeCalculation() {
        PlayerState player = new PlayerState(1L, 50.0, 50.0);

        // Move UP (dy = -1.0, step = 2.0) -> y = 48.0
        MovementService.applyMovement(player, Direction.UP);
        assertThat(player.getX()).isEqualTo(50.0);
        assertThat(player.getY()).isEqualTo(48.0);

        // Move RIGHT (dx = 1.0, step = 2.0) -> x = 52.0
        MovementService.applyMovement(player, Direction.RIGHT);
        assertThat(player.getX()).isEqualTo(52.0);
        assertThat(player.getY()).isEqualTo(48.0);

        // Move DOWN (dy = 1.0, step = 2.0) -> y = 50.0
        MovementService.applyMovement(player, Direction.DOWN);
        assertThat(player.getX()).isEqualTo(52.0);
        assertThat(player.getY()).isEqualTo(50.0);

        // Move LEFT (dx = -1.0, step = 2.0) -> x = 50.0
        MovementService.applyMovement(player, Direction.LEFT);
        assertThat(player.getX()).isEqualTo(50.0);
        assertThat(player.getY()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("탈락한 플레이어는 방향 명령이 주어져도 이동할 수 없다")
    void testDeadPlayerMoveRejected() {
        PlayerState player = new PlayerState(2L, 50.0, 50.0);
        player.eliminate();

        assertThatThrownBy(() -> MovementService.applyMovement(player, Direction.UP))
                .isInstanceOf(IllegalStateException.class);
    }
}
