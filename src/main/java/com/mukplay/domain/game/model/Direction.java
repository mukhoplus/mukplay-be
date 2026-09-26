package com.mukplay.domain.game.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Direction {
    UP(0.0, -1.0),
    DOWN(0.0, 1.0),
    LEFT(-1.0, 0.0),
    RIGHT(1.0, 0.0),
    UP_LEFT(-0.7071, -0.7071),
    UP_RIGHT(0.7071, -0.7071),
    DOWN_LEFT(-0.7071, 0.7071),
    DOWN_RIGHT(0.7071, 0.7071);

    private final double dx;
    private final double dy;
}
