package com.mukplay.domain.room.model;

import com.mukplay.common.exception.BusinessException;
import com.mukplay.common.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class Room {

    private final String roomId;
    private final String name;
    private Long hostId;
    private final int maxPlayers;
    private RoomState state;
    private final List<RoomParticipant> participants;

    @Builder
    public Room(String roomId, String name, Long hostId, int maxPlayers) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "방 이름을 입력해주세요.");
        }
        if (maxPlayers < 2 || maxPlayers > 50) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "방 정원은 2명 이상 50명 이하여야 합니다.");
        }
        this.roomId = roomId;
        this.name = name;
        this.hostId = hostId;
        this.maxPlayers = maxPlayers;
        this.state = RoomState.WAITING;
        this.participants = new ArrayList<>();
    }

    public synchronized void addParticipant(RoomParticipant participant) {
        if (this.state != RoomState.WAITING) {
            throw new BusinessException(ErrorCode.ROOM_NOT_WAITING);
        }
        if (this.participants.size() >= this.maxPlayers) {
            throw new BusinessException(ErrorCode.ROOM_FULL);
        }
        boolean duplicate = this.participants.stream()
                .anyMatch(p -> Objects.equals(p.getUserId(), participant.getUserId()));
        if (duplicate) {
            throw new BusinessException(ErrorCode.ALREADY_JOINED);
        }
        this.participants.add(participant);
    }

    public synchronized void removeParticipant(Long userId) {
        boolean removed = this.participants.removeIf(p -> Objects.equals(p.getUserId(), userId));
        if (!removed) {
            return;
        }

        // Host migration
        if (Objects.equals(this.hostId, userId) && !this.participants.isEmpty()) {
            this.hostId = this.participants.get(0).getUserId();
        }
    }

    public synchronized void start(Long requesterId) {
        if (!Objects.equals(this.hostId, requesterId)) {
            throw new BusinessException(ErrorCode.NOT_ROOM_HOST);
        }
        if (this.state != RoomState.WAITING) {
            throw new BusinessException(ErrorCode.ROOM_NOT_WAITING);
        }
        if (this.participants.size() < 2) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "최소 2명 이상이어야 게임을 시작할 수 있습니다.");
        }
        this.state = RoomState.PLAYING;
    }

    public boolean isEmpty() {
        return this.participants.isEmpty();
    }
}
