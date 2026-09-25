package com.mukplay.domain.game.entity;

import com.mukplay.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false, length = 100)
    private String roomId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at", nullable = false)
    private LocalDateTime finishedAt;

    @Column(name = "participant_count", nullable = false)
    private int participantCount;

    @Builder
    public GameLog(String roomId, LocalDateTime startedAt, LocalDateTime finishedAt, int participantCount) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId는 필수입니다.");
        }
        if (startedAt == null) {
            throw new IllegalArgumentException("startedAt은 필수입니다.");
        }
        if (finishedAt == null) {
            throw new IllegalArgumentException("finishedAt은 필수입니다.");
        }
        if (participantCount < 0) {
            throw new IllegalArgumentException("participantCount는 0 이상이어야 합니다.");
        }

        this.roomId = roomId;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.participantCount = participantCount;
    }
}
