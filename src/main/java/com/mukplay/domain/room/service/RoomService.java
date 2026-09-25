package com.mukplay.domain.room.service;

import com.mukplay.common.exception.BusinessException;
import com.mukplay.common.exception.ErrorCode;
import com.mukplay.domain.room.dto.CreateRoomRequest;
import com.mukplay.domain.room.dto.RoomResponse;
import com.mukplay.domain.room.model.Room;
import com.mukplay.domain.room.model.RoomParticipant;
import com.mukplay.domain.room.repository.RoomRedisRepository;
import com.mukplay.domain.user.entity.User;
import com.mukplay.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRedisRepository roomRedisRepository;
    private final UserRepository userRepository;

    public RoomResponse createRoom(Long userId, CreateRoomRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        String roomId = UUID.randomUUID().toString().substring(0, 8);
        Room room = Room.builder()
                .roomId(roomId)
                .name(request.name())
                .hostId(userId)
                .maxPlayers(request.maxPlayers())
                .build();

        room.addParticipant(new RoomParticipant(user.getId(), user.getNickname()));
        roomRedisRepository.save(room);

        return RoomResponse.from(room);
    }

    public List<RoomResponse> getWaitingRooms() {
        return roomRedisRepository.findAllWaiting().stream()
                .map(RoomResponse::from)
                .toList();
    }

    public RoomResponse getRoom(String roomId) {
        Room room = roomRedisRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        return RoomResponse.from(room);
    }

    public RoomResponse joinRoom(Long userId, String roomId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        Room room = roomRedisRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        room.addParticipant(new RoomParticipant(user.getId(), user.getNickname()));
        roomRedisRepository.save(room);

        return RoomResponse.from(room);
    }

    public RoomResponse leaveRoom(Long userId, String roomId) {
        Room room = roomRedisRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        room.removeParticipant(userId);
        if (room.isEmpty()) {
            roomRedisRepository.deleteById(roomId);
            return null;
        } else {
            roomRedisRepository.save(room);
            return RoomResponse.from(room);
        }
    }

    public RoomResponse startRoom(Long userId, String roomId) {
        Room room = roomRedisRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        room.start(userId);
        roomRedisRepository.save(room);

        return RoomResponse.from(room);
    }
}
