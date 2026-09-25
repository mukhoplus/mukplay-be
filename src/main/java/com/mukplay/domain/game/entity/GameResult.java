package com.mukplay.domain.game.entity;

import com.mukplay.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "game_results",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_game_result_log_user", columnNames = {"game_log_id", "user_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameResult extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_log_id", nullable = false)
    private Long gameLogId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "game_rank", nullable = false)
    private int rank;

    @Column(name = "earned_exp", nullable = false)
    private int earnedExp;

    @Column(name = "survived_rounds", nullable = false)
    private int survivedRounds;

    @Builder
    public GameResult(Long gameLogId, Long userId, int rank, int earnedExp, int survivedRounds) {
        if (gameLogId == null) {
            throw new IllegalArgumentException("gameLogId는 필수입니다.");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }
        if (rank <= 0) {
            throw new IllegalArgumentException("rank는 1 이상이어야 합니다.");
        }
        if (earnedExp < 0) {
            throw new IllegalArgumentException("earnedExp는 0 이상이어야 합니다.");
        }
        if (survivedRounds < 0) {
            throw new IllegalArgumentException("survivedRounds는 0 이상이어야 합니다.");
        }

        this.gameLogId = gameLogId;
        this.userId = userId;
        this.rank = rank;
        this.earnedExp = earnedExp;
        this.survivedRounds = survivedRounds;
    }
}
