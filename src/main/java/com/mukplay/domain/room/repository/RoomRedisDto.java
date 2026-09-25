package com.mukplay.domain.room.repository;

import com.mukplay.domain.room.model.Room;
import com.mukplay.domain.room.model.RoomParticipant;
import com.mukplay.domain.room.model.RoomState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomRedisDto {

    private String roomId;
    private String name;
    private Long hostId;
    private int maxPlayers;
    private RoomState state;
    private List<ParticipantDto> participants;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantDto {
        private Long userId;
        private String nickname;
        private LocalDateTime joinedAt;

        public static ParticipantDto from(RoomParticipant p) {
            return new ParticipantDto(p.getUserId(), p.getNickname(), p.getJoinedAt());
        }

        public RoomParticipant toDomain() {
            return new RoomParticipant(this.userId, this.nickname);
        }
    }

    public static RoomRedisDto from(Room room) {
        List<ParticipantDto> pList = room.getParticipants().stream()
                .map(ParticipantDto::from)
                .toList();

        return RoomRedisDto.builder()
                .roomId(room.getRoomId())
                .name(room.getName())
                .hostId(room.getHostId())
                .maxPlayers(room.getMaxPlayers())
                .state(room.getState())
                .participants(pList)
                .build();
    }

    public Room toDomain() {
        Room room = Room.builder()
                .roomId(this.roomId)
                .name(this.name)
                .hostId(this.hostId)
                .maxPlayers(this.maxPlayers)
                .build();

        if (this.participants != null) {
            for (ParticipantDto p : this.participants) {
                room.addParticipant(p.toDomain());
            }
        }
        return room;
    }
}
