package com.mukplay.websocket.dto;

import com.mukplay.domain.game.model.GameSession;
import com.mukplay.domain.game.model.GameSessionState;
import com.mukplay.domain.game.model.Round;

import java.time.Instant;

/**
 * Payload broadcasted to /topic/room/{roomId}/state
 */
public record GameStateBroadcast(
        String roomId,
        GameSessionState state,
        int currentRound,
        int maxRounds,
        Long questionId,
        Instant startedAt,
        Instant endsAt,
        long alivePlayerCount
) {
    public static GameStateBroadcast from(GameSession session, Round currentRound) {
        return new GameStateBroadcast(
                session.getRoomId(),
                session.getState(),
                session.getCurrentRound(),
                session.getMaxRounds(),
                currentRound != null ? currentRound.getQuestionId() : null,
                currentRound != null ? currentRound.getStartedAt() : session.getStartedAt(),
                currentRound != null ? currentRound.getEndsAt() : null,
                session.getAlivePlayerCount()
        );
    }
}
