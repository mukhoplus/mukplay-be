package com.mukplay.domain.user.dto;

import com.mukplay.domain.user.entity.User;

public record UserRankingResponse(
        int rank,
        Long userId,
        String nickname,
        int level,
        int exp
) {
    public static UserRankingResponse of(int rank, User user) {
        return new UserRankingResponse(
                rank,
                user.getId(),
                user.getNickname(),
                user.getLevel(),
                user.getExp()
        );
    }
}
