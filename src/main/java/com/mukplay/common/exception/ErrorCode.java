package com.mukplay.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C004", "요청한 리소스를 찾을 수 없습니다."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "만료된 토큰입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "A005", "아이디 또는 비밀번호가 올바르지 않습니다."),
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "A006", "이미 존재하는 로그인 아이디입니다."),

    // Room
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "존재하지 않는 방입니다."),
    ROOM_FULL(HttpStatus.BAD_REQUEST, "R002", "방 정원이 초과되었습니다."),
    ROOM_NOT_WAITING(HttpStatus.BAD_REQUEST, "R003", "이미 시작되었거나 종료된 방입니다."),
    NOT_ROOM_HOST(HttpStatus.FORBIDDEN, "R004", "방장만 가능한 권한입니다."),
    ALREADY_JOINED(HttpStatus.CONFLICT, "R005", "이미 참가 중인 방입니다."),

    // Game Core
    INVALID_GAME_STATE(HttpStatus.BAD_REQUEST, "G001", "유효하지 않은 게임 상태입니다."),
    PLAYER_NOT_ALIVE(HttpStatus.BAD_REQUEST, "G002", "탈락한 플레이어는 이동할 수 없습니다."),
    INVALID_DIRECTION(HttpStatus.BAD_REQUEST, "G003", "유효하지 않은 방향 명령입니다."),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "G004", "요청 횟수 제한을 초과했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
