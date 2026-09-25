package com.mukplay.domain.game.model;

import lombok.Getter;

import java.time.Instant;
import java.util.*;

@Getter
public class GameSession {

    private final String roomId;
    private GameSessionState state;
    private final Map<Long, PlayerState> players;
    private int currentRound;
    private final int maxRounds;
    private final Instant startedAt;

    public GameSession(String roomId, int maxRounds) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId는 필수입니다.");
        }
        if (maxRounds <= 0) {
            throw new IllegalArgumentException("maxRounds는 1 이상이어야 합니다.");
        }
        this.roomId = roomId;
        this.state = GameSessionState.WAITING;
        this.players = new LinkedHashMap<>();
        this.currentRound = 0;
        this.maxRounds = maxRounds;
        this.startedAt = Instant.now();
    }

    public synchronized void transitionTo(GameSessionState newState) {
        if (!isValidTransition(this.state, newState)) {
            throw new IllegalStateException(String.format("잘못된 상태 전이입니다: %s -> %s", this.state, newState));
        }
        this.state = newState;
    }

    private boolean isValidTransition(GameSessionState from, GameSessionState to) {
        return switch (from) {
            case WAITING -> to == GameSessionState.STARTING;
            case STARTING -> to == GameSessionState.PLAYING;
            case PLAYING -> to == GameSessionState.ROUND_END || to == GameSessionState.FINISHED;
            case ROUND_END -> to == GameSessionState.PLAYING || to == GameSessionState.FINISHED;
            case FINISHED -> false;
        };
    }

    public synchronized void addPlayer(PlayerState player) {
        if (player == null) {
            throw new IllegalArgumentException("player는 필수입니다.");
        }
        this.players.put(player.getUserId(), player);
    }

    public synchronized PlayerState getPlayer(Long userId) {
        return this.players.get(userId);
    }

    public synchronized void nextRound() {
        if (this.currentRound >= this.maxRounds) {
            transitionTo(GameSessionState.FINISHED);
            return;
        }
        this.currentRound++;
    }
}
