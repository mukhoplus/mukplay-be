package com.mukplay.websocket.dto;

import com.mukplay.domain.game.model.GameSession;
import com.mukplay.domain.game.model.PlayerState;

import java.time.Instant;
import java.util.List;

/**
 * Payload broadcasted to /topic/room/{roomId}/positions
 */
public record RoomPositionsBroadcast(
        String roomId,
        long timestamp,
        List<PlayerPositionDto> positions
) {
    public record PlayerPositionDto(
            Long userId,
            double x,
            double y,
            boolean alive
    ) {
        public static PlayerPositionDto from(PlayerState player) {
            return new PlayerPositionDto(
                    player.getUserId(),
                    player.getX(),
                    player.getY(),
                    player.isAlive()
            );
        }
    }

    public static RoomPositionsBroadcast from(GameSession session) {
        List<PlayerPositionDto> playerPositions = session.getPlayers().values().stream()
                .map(PlayerPositionDto::from)
                .toList();

        return new RoomPositionsBroadcast(
                session.getRoomId(),
                Instant.now().toEpochMilli(),
                playerPositions
        );
    }
}
