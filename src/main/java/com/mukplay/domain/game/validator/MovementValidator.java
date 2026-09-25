package com.mukplay.domain.game.validator;

import com.mukplay.domain.game.model.Direction;
import com.mukplay.domain.game.model.GameSession;
import com.mukplay.domain.game.model.GameSessionState;
import com.mukplay.domain.game.model.PlayerState;

public class MovementValidator {

    public static final double MIN_X = 0.0;
    public static final double MAX_X = 100.0;
    public static final double MIN_Y = 0.0;
    public static final double MAX_Y = 100.0;

    public static void validateMove(GameSession session, PlayerState player, Direction direction) {
        if (session == null || session.getState() != GameSessionState.PLAYING) {
            throw new IllegalStateException("게임 진행(PLAYING) 상태에서만 이동할 수 있습니다.");
        }
        if (player == null || !player.isAlive()) {
            throw new IllegalStateException("탈락한 플레이어는 이동할 수 없습니다.");
        }
        if (direction == null) {
            throw new IllegalArgumentException("방향이 지정되지 않았습니다.");
        }
    }

    public static double clampX(double x) {
        return Math.max(MIN_X, Math.min(MAX_X, x));
    }

    public static double clampY(double y) {
        return Math.max(MIN_Y, Math.min(MAX_Y, y));
    }
}
