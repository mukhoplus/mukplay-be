package com.mukplay.websocket.dto;

import com.mukplay.domain.game.model.Direction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MoveRequest(
        @NotBlank(message = "roomId는 필수입니다.")
        String roomId,

        @NotNull(message = "direction은 필수입니다.")
        Direction direction
) {}
