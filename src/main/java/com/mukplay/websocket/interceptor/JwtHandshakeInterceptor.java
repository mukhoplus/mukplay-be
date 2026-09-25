package com.mukplay.websocket.interceptor;

import com.mukplay.common.exception.BusinessException;
import com.mukplay.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

/**
 * HandshakeInterceptor that extracts and validates JWT token
 * passed via HTTP query parameter (?token=...) or Authorization header during WebSocket handshake.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    public static final String USER_ID_ATTR = "userId";
    public static final String LOGIN_ID_ATTR = "loginId";
    public static final String ROLE_ATTR = "role";

    private final JwtProvider jwtProvider;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

        String token = resolveToken(request);

        if (!StringUtils.hasText(token)) {
            log.warn("WebSocket handshake failed: Missing JWT token");
            return false;
        }

        try {
            if (!jwtProvider.validateToken(token)) {
                log.warn("WebSocket handshake failed: Invalid JWT token");
                return false;
            }

            Long userId = jwtProvider.getUserId(token);
            String loginId = jwtProvider.getLoginId(token);
            String role = jwtProvider.getRole(token).name();

            attributes.put(USER_ID_ATTR, userId);
            attributes.put(LOGIN_ID_ATTR, loginId);
            attributes.put(ROLE_ATTR, role);

            log.info("WebSocket handshake successful: userId={}, loginId={}", userId, loginId);
            return true;
        } catch (BusinessException e) {
            log.warn("WebSocket handshake authentication error: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during WebSocket handshake", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // No-op
    }

    private String resolveToken(ServerHttpRequest request) {
        // 1. Authorization header (Bearer ...)
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2. Query param ?token=...
        URI uri = request.getURI();
        String query = uri.getQuery();
        if (StringUtils.hasText(query)) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair.length == 2 && "token".equals(pair[0])) {
                    return pair[1];
                }
            }
        }

        return null;
    }
}
