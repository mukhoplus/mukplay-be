package com.mukplay.domain.game.model;

import com.mukplay.domain.game.command.MoveCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DirectionTest {

    @Test
    @DisplayName("Direction의 dx, dy 델타값이 올바르게 정의되어 있어야 한다")
    void testDirectionDeltas() {
        assertThat(Direction.UP.getDx()).isEqualTo(0.0);
        assertThat(Direction.UP.getDy()).isEqualTo(-1.0);

        assertThat(Direction.DOWN.getDx()).isEqualTo(0.0);
        assertThat(Direction.DOWN.getDy()).isEqualTo(1.0);

        assertThat(Direction.LEFT.getDx()).isEqualTo(-1.0);
        assertThat(Direction.LEFT.getDy()).isEqualTo(0.0);

        assertThat(Direction.RIGHT.getDx()).isEqualTo(1.0);
        assertThat(Direction.RIGHT.getDy()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("MoveCommand는 필수 필드가 누락되면 IllegalArgumentException을 던져야 한다")
    void testMoveCommandValidation() {
        MoveCommand valid = new MoveCommand("room-1", 10L, Direction.UP);
        assertThat(valid.roomId()).isEqualTo("room-1");
        assertThat(valid.userId()).isEqualTo(10L);
        assertThat(valid.direction()).isEqualTo(Direction.UP);

        assertThatThrownBy(() -> new MoveCommand(null, 10L, Direction.UP))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new MoveCommand("room-1", null, Direction.UP))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new MoveCommand("room-1", 10L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
