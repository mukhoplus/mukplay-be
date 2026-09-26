package com.mukplay.domain.game.model;

import lombok.Getter;

import java.time.Instant;

@Getter
public class PlayerState {

    private final Long userId;
    private final String nickname;
    private double x;
    private double y;
    private boolean alive;
    private int correctCount;
    private int wrongCount;
    private int survivedRounds;
    private Integer eliminatedRound;
    private final Instant joinedAt;

    public PlayerState(Long userId, String nickname, double initialX, double initialY) {
        this.userId = userId;
        this.nickname = nickname != null ? nickname : "플레이어 " + userId;
        this.x = initialX;
        this.y = initialY;
        this.alive = true;
        this.correctCount = 0;
        this.wrongCount = 0;
        this.survivedRounds = 0;
        this.joinedAt = Instant.now();
    }

    public PlayerState(Long userId, double initialX, double initialY) {
        this(userId, "플레이어 " + userId, initialX, initialY);
    }

    public synchronized void moveTo(double newX, double newY) {
        if (!this.alive) {
            throw new IllegalStateException("탈락한 플레이어는 이동할 수 없습니다.");
        }
        this.x = newX;
        this.y = newY;
    }

    public synchronized void recordCorrect() {
        this.correctCount++;
        this.survivedRounds++;
    }

    public synchronized void recordWrong(int round) {
        this.wrongCount++;
        this.eliminatedRound = round;
        this.alive = false;
    }

    public synchronized void eliminate() {
        this.alive = false;
    }
}
