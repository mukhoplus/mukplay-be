package com.mukplay.domain.room.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoomRequest(
        @NotBlank(message = "방 이름을 입력해주세요.")
        @Size(min = 2, max = 30, message = "방 이름은 2자 이상 30자 이하여야 합니다.")
        String name,

        @Min(value = 2, message = "최소 인원은 2명입니다.")
        @Max(value = 50, message = "최대 인원은 50명입니다.")
        int maxPlayers
) {
}
