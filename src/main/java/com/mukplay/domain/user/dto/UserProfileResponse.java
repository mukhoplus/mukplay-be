package com.mukplay.domain.user.dto;

import com.mukplay.domain.user.entity.User;
import com.mukplay.domain.user.entity.UserRole;

public record UserProfileResponse(
        Long id,
        String loginId,
        String nickname,
        int level,
        int exp,
        UserRole role
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getLoginId(),
                user.getNickname(),
                user.getLevel(),
                user.getExp(),
                user.getRole()
        );
    }
}
