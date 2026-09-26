package com.mukplay.domain.game.dto;

import java.util.List;

public record GameResultResponse(
        String roomId,
        int participantCount,
        PlayerResultDto myResult,
        List<PlayerResultDto> rankings
) {
    public record PlayerResultDto(
            Long userId,
            String nickname,
            int rank,
            int correctCount,
            int wrongCount,
            int survivedRounds,
            int earnedExp,
            int currentLevel,
            int currentExp,
            int nextLevelExp,
            int expProgressPercent,
            boolean isWinner
    ) {}
}
