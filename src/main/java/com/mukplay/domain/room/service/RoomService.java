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
import com.mukplay.domain.game.model.GameSession;
import com.mukplay.domain.game.model.GameSessionState;
import com.mukplay.domain.game.model.PlayerState;
import com.mukplay.domain.game.repository.GameSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRedisRepository roomRedisRepository;
    private final UserRepository userRepository;
    private final GameSessionRepository gameSessionRepository;

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

    public RoomResponse addBot(String roomId) {
        Room room = roomRedisRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        long botId = 99000L + (room.getParticipants().size() * 10L) + (System.currentTimeMillis() % 100);
        String botNickname = "테스트봇" + room.getParticipants().size();
        room.addParticipant(new RoomParticipant(botId, botNickname));
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

        // Active GameSession 생성 및 등록
        GameSession session = new GameSession(room.getRoomId(), 10);
        int index = 0;
        int total = room.getParticipants().size();
        for (RoomParticipant p : room.getParticipants()) {
            double initialX = 50.0 + ((index - (total / 2.0)) * 6.0);
            double initialY = 50.0;
            session.addPlayer(new PlayerState(p.getUserId(), initialX, initialY));
            index++;
        }
        session.transitionTo(GameSessionState.STARTING);
        session.transitionTo(GameSessionState.PLAYING);
        session.nextRound();
        gameSessionRepository.save(session);

        return RoomResponse.from(room);
    }
}
