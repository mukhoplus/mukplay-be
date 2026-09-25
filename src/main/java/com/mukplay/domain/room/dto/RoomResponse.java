package com.mukplay.domain.room.dto;

import com.mukplay.domain.room.model.Room;
import com.mukplay.domain.room.model.RoomParticipant;
import com.mukplay.domain.room.model.RoomState;

import java.time.LocalDateTime;
import java.util.List;

public record RoomResponse(
        String roomId,
        String name,
        Long hostId,
        int currentPlayers,
        int maxPlayers,
        RoomState state,
        List<ParticipantResponse> participants
) {
    public record ParticipantResponse(Long userId, String nickname, LocalDateTime joinedAt) {
        public static ParticipantResponse from(RoomParticipant p) {
            return new ParticipantResponse(p.getUserId(), p.getNickname(), p.getJoinedAt());
        }
    }

    public static RoomResponse from(Room room) {
        List<ParticipantResponse> pList = room.getParticipants().stream()
                .map(ParticipantResponse::from)
                .toList();

        return new RoomResponse(
                room.getRoomId(),
                room.getName(),
                room.getHostId(),
                room.getParticipants().size(),
                room.getMaxPlayers(),
                room.getState(),
                pList
        );
    }
}
