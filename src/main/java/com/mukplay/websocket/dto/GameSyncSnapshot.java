package com.mukplay.websocket.dto;

import com.mukplay.domain.game.model.GameSession;
import com.mukplay.domain.game.model.GameSessionState;
import com.mukplay.domain.game.model.Round;

import java.time.Instant;
import java.util.List;

/**
 * Full game state snapshot delivered upon client connection or reconnection.
 */
public record GameSyncSnapshot(
        String roomId,
        GameSessionState state,
        int currentRound,
        int maxRounds,
        Long questionId,
        Instant roundStartedAt,
        Instant roundEndsAt,
        long serverTime,
        List<RoomPositionsBroadcast.PlayerPositionDto> positions
) {
    public static GameSyncSnapshot of(GameSession session, Round currentRound) {
        List<RoomPositionsBroadcast.PlayerPositionDto> positionDtos = session.getPlayers().values().stream()
                .map(RoomPositionsBroadcast.PlayerPositionDto::from)
                .toList();

        return new GameSyncSnapshot(
                session.getRoomId(),
                session.getState(),
                session.getCurrentRound(),
                session.getMaxRounds(),
                currentRound != null ? currentRound.getQuestionId() : null,
                currentRound != null ? currentRound.getStartedAt() : null,
                currentRound != null ? currentRound.getEndsAt() : null,
                Instant.now().toEpochMilli(),
                positionDtos
        );
    }
}
