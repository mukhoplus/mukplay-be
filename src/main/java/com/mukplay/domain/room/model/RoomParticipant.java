package com.mukplay.domain.room.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RoomParticipant {

    private final Long userId;
    private final String nickname;
    private final LocalDateTime joinedAt;

    public RoomParticipant(Long userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
        this.joinedAt = LocalDateTime.now();
    }
}
