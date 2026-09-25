package com.mukplay.domain.game.service;

import com.mukplay.domain.game.command.MoveCommand;
import com.mukplay.domain.game.model.Direction;
import com.mukplay.domain.game.model.PlayerState;

public class MovementService {

    public static final double DEFAULT_STEP_SIZE = 2.0;

    public static void applyMovement(PlayerState player, Direction direction) {
        applyMovement(player, direction, DEFAULT_STEP_SIZE);
    }

    public static void applyMovement(PlayerState player, Direction direction, double stepSize) {
        if (!player.isAlive()) {
            throw new IllegalStateException("탈락한 플레이어는 이동할 수 없습니다.");
        }

        double nextX = player.getX() + (direction.getDx() * stepSize);
        double nextY = player.getY() + (direction.getDy() * stepSize);

        player.moveTo(nextX, nextY);
    }
}
