package com.mukplay.domain.game.dto;

public record PlayerSettlementInfo(
        Long userId,
        String nickname,
        int rank,
        int survivedRounds,
        int correctCount,
        int wrongCount,
        int baseExpSum,
        boolean isWinner
) {
    public PlayerSettlementInfo(Long userId, int rank, int survivedRounds, int baseExpSum, boolean isWinner) {
        this(userId, "플레이어 " + userId, rank, survivedRounds, survivedRounds, 0, baseExpSum, isWinner);
    }
}
