package com.mukplay.domain.game.model;

import lombok.Getter;

import java.time.Instant;

@Getter
public class PlayerState {

    private final Long userId;
    private double x;
    private double y;
    private boolean alive;
    private final Instant joinedAt;

    public PlayerState(Long userId, double initialX, double initialY) {
        this.userId = userId;
        this.x = initialX;
        this.y = initialY;
        this.alive = true;
        this.joinedAt = Instant.now();
    }

    public synchronized void moveTo(double newX, double newY) {
        if (!this.alive) {
            throw new IllegalStateException("탈락한 플레이어는 이동할 수 없습니다.");
        }
        this.x = newX;
        this.y = newY;
    }

    public synchronized void eliminate() {
        this.alive = false;
    }
}
