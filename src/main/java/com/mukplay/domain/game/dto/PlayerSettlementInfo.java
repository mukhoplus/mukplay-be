package com.mukplay.domain.game.dto;

public record PlayerSettlementInfo(
        Long userId,
        int rank,
        int survivedRounds,
        int baseExpSum,
        boolean isWinner
) {}
