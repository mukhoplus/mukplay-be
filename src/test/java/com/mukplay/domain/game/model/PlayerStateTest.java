package com.mukplay.domain.game.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlayerStateTest {

    @Test
    @DisplayName("생존 상태의 플레이어는 위치를 갱신할 수 있어야 한다")
    void testMoveAlivePlayer() {
        PlayerState player = new PlayerState(1L, 50.0, 50.0);

        assertThat(player.isAlive()).isTrue();
        assertThat(player.getX()).isEqualTo(50.0);
        assertThat(player.getY()).isEqualTo(50.0);

        player.moveTo(55.5, 45.0);
        assertThat(player.getX()).isEqualTo(55.5);
        assertThat(player.getY()).isEqualTo(45.0);
    }

    @Test
    @DisplayName("탈락(eliminate)된 플레이어는 이동 시 예외가 발생해야 한다")
    void testDeadPlayerCannotMove() {
        PlayerState player = new PlayerState(2L, 50.0, 50.0);
        player.eliminate();

        assertThat(player.isAlive()).isFalse();

        assertThatThrownBy(() -> player.moveTo(60.0, 60.0))
                .isInstanceOf(IllegalStateException.class);
    }
}
