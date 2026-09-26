package com.mukplay.domain.room.controller;

import com.mukplay.common.response.ApiResponse;
import com.mukplay.domain.room.dto.CreateRoomRequest;
import com.mukplay.domain.room.dto.RoomResponse;
import com.mukplay.domain.room.service.RoomService;
import com.mukplay.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final com.mukplay.domain.game.service.GameResultQueryService gameResultQueryService;

    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateRoomRequest request) {
        RoomResponse response = roomService.createRoom(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("방이 생성되었습니다.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getWaitingRooms() {
        List<RoomResponse> rooms = roomService.getWaitingRooms();
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoom(@PathVariable String roomId) {
        RoomResponse room = roomService.getRoom(roomId);
        return ResponseEntity.ok(ApiResponse.success(room));
    }

    @GetMapping("/{roomId}/game")
    public ResponseEntity<ApiResponse<com.mukplay.domain.room.dto.CurrentGameResponse>> getCurrentGame(@PathVariable String roomId) {
        com.mukplay.domain.room.dto.CurrentGameResponse game = roomService.getCurrentGame(roomId);
        return ResponseEntity.ok(ApiResponse.success(game));
    }

    @GetMapping("/{roomId}/result")
    public ResponseEntity<ApiResponse<com.mukplay.domain.game.dto.GameResultResponse>> getGameResult(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String roomId) {
        Long userId = principal != null ? principal.getId() : null;
        com.mukplay.domain.game.dto.GameResultResponse result = gameResultQueryService.getGameResult(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<ApiResponse<RoomResponse>> joinRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String roomId) {
        RoomResponse response = roomService.joinRoom(principal.getId(), roomId);
        return ResponseEntity.ok(ApiResponse.success("방에 입장했습니다.", response));
    }

    @PostMapping("/{roomId}/bot")
    public ResponseEntity<ApiResponse<RoomResponse>> addBot(
            @PathVariable String roomId) {
        RoomResponse response = roomService.addBot(roomId);
        return ResponseEntity.ok(ApiResponse.success("테스트봇이 입장했습니다.", response));
    }

    @PostMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse<RoomResponse>> leaveRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String roomId) {
        RoomResponse response = roomService.leaveRoom(principal.getId(), roomId);
        return ResponseEntity.ok(ApiResponse.success("방에서 퇴장했습니다.", response));
    }

    @PostMapping("/{roomId}/start")
    public ResponseEntity<ApiResponse<RoomResponse>> startRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String roomId) {
        RoomResponse response = roomService.startRoom(principal.getId(), roomId);
        return ResponseEntity.ok(ApiResponse.success("게임을 시작합니다.", response));
    }
}
