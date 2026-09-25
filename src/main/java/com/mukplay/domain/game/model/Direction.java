package com.mukplay.domain.game.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Direction {
    UP(0.0, -1.0),
    DOWN(0.0, 1.0),
    LEFT(-1.0, 0.0),
    RIGHT(1.0, 0.0);

    private final double dx;
    private final double dy;
}
