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

    @PostMapping("/{roomId}/join")
    public ResponseEntity<ApiResponse<RoomResponse>> joinRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String roomId) {
        RoomResponse response = roomService.joinRoom(principal.getId(), roomId);
        return ResponseEntity.ok(ApiResponse.success("방에 입장했습니다.", response));
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
