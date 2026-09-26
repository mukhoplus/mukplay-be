package com.mukplay.websocket.controller;

import com.mukplay.common.exception.BusinessException;
import com.mukplay.common.exception.ErrorCode;
import com.mukplay.domain.game.command.MoveCommand;
import com.mukplay.domain.game.model.GameSession;
import com.mukplay.domain.game.model.PlayerState;
import com.mukplay.domain.game.repository.GameSessionRepository;
import com.mukplay.domain.game.service.MovementService;
import com.mukplay.websocket.dto.MoveRequest;
import com.mukplay.websocket.interceptor.JwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * Controller handling real-time WebSocket / STOMP game messages.
 */
@Slf4j
@Controller
public class GameMessageController {

    private final GameSessionRepository gameSessionRepository;
    private final MovementService movementService;
    private final com.mukplay.domain.game.service.MovementRateLimiter rateLimiter;
    private final com.mukplay.websocket.service.GamePositionBroadcastService gamePositionBroadcastService;

    @org.springframework.beans.factory.annotation.Autowired
    public GameMessageController(
            GameSessionRepository gameSessionRepository,
            MovementService movementService,
            com.mukplay.domain.game.service.MovementRateLimiter rateLimiter,
            com.mukplay.websocket.service.GamePositionBroadcastService gamePositionBroadcastService) {
        this.gameSessionRepository = gameSessionRepository;
        this.movementService = movementService;
        this.rateLimiter = rateLimiter;
        this.gamePositionBroadcastService = gamePositionBroadcastService;
    }

    public GameMessageController(
            GameSessionRepository gameSessionRepository,
            MovementService movementService,
            com.mukplay.domain.game.service.MovementRateLimiter rateLimiter) {
        this(gameSessionRepository, movementService, rateLimiter, null);
    }

    @MessageMapping("/game/move")
    public void handleMove(
            @Payload MoveRequest request,
            SimpMessageHeaderAccessor headerAccessor) {

        Long userId = extractUserId(headerAccessor);
        if (userId == null) {
            log.warn("Unauthorized move attempt: No userId in session attributes");
            return;
        }

        if (rateLimiter != null && !rateLimiter.isAllowed(userId)) {
            log.warn("Move rate limit exceeded for user: {}", userId);
            return;
        }

        MoveCommand command = new MoveCommand(request.roomId(), userId, request.direction());
        dispatchMove(command);
    }

    public void dispatchMove(MoveCommand command) {
        GameSession session = gameSessionRepository.findByRoomId(command.roomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "게임을 찾을 수 없습니다. roomId=" + command.roomId()));

        PlayerState player = session.getPlayer(command.userId());
        if (player == null) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "해당 게임 세션에 참가하지 않은 플레이어입니다. userId=" + command.userId());
        }

        movementService.move(session, player, command.direction());
        if (gamePositionBroadcastService != null) {
            gamePositionBroadcastService.broadcastPositions(session);
        }
        log.debug("Move applied and broadcasted: roomId={}, userId={}, dir={}, newPos=({}, {})",
                command.roomId(), command.userId(), command.direction(), player.getX(), player.getY());
    }

    private Long extractUserId(SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return null;
        }
        Object userIdObj = sessionAttributes.get(JwtHandshakeInterceptor.USER_ID_ATTR);
        if (userIdObj instanceof Long l) {
            return l;
        } else if (userIdObj instanceof Number n) {
            return n.longValue();
        } else if (userIdObj instanceof String s) {
            return Long.parseLong(s);
        }
        return null;
    }
}
