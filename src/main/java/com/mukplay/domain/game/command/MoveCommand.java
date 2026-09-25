package com.mukplay.domain.game.command;

import com.mukplay.domain.game.model.Direction;

public record MoveCommand(
        String roomId,
        Long userId,
        Direction direction
) {
    public MoveCommand {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId는 필수입니다.");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }
        if (direction == null) {
            throw new IllegalArgumentException("direction은 필수입니다.");
        }
    }
}
