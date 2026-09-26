package com.mukplay.domain.room.dto;

import com.mukplay.domain.game.model.GameSessionState;
import com.mukplay.websocket.dto.RoomPositionsBroadcast;

import java.time.Instant;
import java.util.List;

public record CurrentGameResponse(
        String roomId,
        GameSessionState state,
        int currentRound,
        int maxRounds,
        Long questionId,
        String questionContent,
        Instant startedAt,
        Instant endsAt,
        long alivePlayerCount,
        List<RoomPositionsBroadcast.PlayerPositionDto> positions
) {
}
