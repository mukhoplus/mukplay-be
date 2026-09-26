package com.mukplay.domain.room.service;

import com.mukplay.common.exception.BusinessException;
import com.mukplay.common.exception.ErrorCode;
import com.mukplay.domain.game.model.*;
import com.mukplay.domain.game.repository.GameSessionRepository;
import com.mukplay.domain.question.entity.Question;
import com.mukplay.domain.question.entity.QuestionDifficulty;
import com.mukplay.domain.question.entity.QuestionStatus;
import com.mukplay.domain.question.repository.QuestionRepository;
import com.mukplay.domain.room.dto.CreateRoomRequest;
import com.mukplay.domain.room.dto.CurrentGameResponse;
import com.mukplay.domain.room.dto.RoomResponse;
import com.mukplay.domain.room.model.Room;
import com.mukplay.domain.room.model.RoomParticipant;
import com.mukplay.domain.room.repository.RoomRedisRepository;
import com.mukplay.domain.user.entity.User;
import com.mukplay.domain.user.repository.UserRepository;
import com.mukplay.websocket.dto.RoomPositionsBroadcast;
import com.mukplay.websocket.service.GamePositionBroadcastService;
import com.mukplay.websocket.service.GameStateBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRedisRepository roomRedisRepository;
    private final UserRepository userRepository;
    private final GameSessionRepository gameSessionRepository;
    private final QuestionRepository questionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameStateBroadcastService gameStateBroadcastService;
    private final GamePositionBroadcastService gamePositionBroadcastService;

    private final Map<String, Round> activeRounds = new ConcurrentHashMap<>();
    private final Map<String, String> activeQuestionContents = new ConcurrentHashMap<>();

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
        GameSession session = new GameSession(room.getRoomId(), 5);
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

        // 1라운드 문제 할당
        Question question = getOrCreateDefaultQuestion();
        Round firstRound = new Round(
                1,
                question.getId(),
                question.getAnswer(),
                Instant.now(),
                Duration.ofSeconds(15)
        );

        activeRounds.put(roomId, firstRound);
        activeQuestionContents.put(roomId, question.getContent());

        // 1. 대기실에 참가 중인 모든 클라이언트에게 게임 시작 브로드캐스트 (실시간 이동 트리거)
        Map<String, Object> startSignal = Map.of(
                "roomId", roomId,
                "state", "PLAYING"
        );
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/start", (Object) startSignal);

        // 2. 게임 상태 및 문제 브로드캐스트
        gameStateBroadcastService.broadcastState(session, firstRound, question.getContent());

        // 3. 플레이어 위치 초기화 브로드캐스트
        gamePositionBroadcastService.broadcastPositions(session);

        log.info("Game started successfully: roomId={}, playerCount={}, question={}",
                roomId, total, question.getContent());

        return RoomResponse.from(room);
    }

    public CurrentGameResponse getCurrentGame(String roomId) {
        GameSession session = gameSessionRepository.findByRoomId(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "현재 진행 중인 게임이 없습니다. roomId=" + roomId));

        Round round = activeRounds.get(roomId);
        String questionContent = activeQuestionContents.get(roomId);

        List<RoomPositionsBroadcast.PlayerPositionDto> positions = session.getPlayers().values().stream()
                .map(RoomPositionsBroadcast.PlayerPositionDto::from)
                .toList();

        return new CurrentGameResponse(
                session.getRoomId(),
                session.getState(),
                session.getCurrentRound(),
                session.getMaxRounds(),
                round != null ? round.getQuestionId() : null,
                questionContent != null ? questionContent : "문제를 불러오는 중입니다...",
                round != null ? round.getStartedAt() : session.getStartedAt(),
                round != null ? round.getEndsAt() : null,
                session.getAlivePlayerCount(),
                positions
        );
    }

    public Round getActiveRound(String roomId) {
        return activeRounds.get(roomId);
    }

    public void updateActiveRound(String roomId, Round round, String questionContent) {
        activeRounds.put(roomId, round);
        activeQuestionContents.put(roomId, questionContent);
    }

    public Question getRandomQuestion() {
        return getOrCreateDefaultQuestion();
    }

    private Question getOrCreateDefaultQuestion() {
        List<Question> approved = questionRepository.findByStatus(QuestionStatus.APPROVED);
        if (!approved.isEmpty()) {
            return approved.get((int) (Math.random() * approved.size()));
        }

        // DB에 문제가 하나도 없을 때 기본 퀴즈 1개 자동 등록
        Question defaultQ = Question.builder()
                .content("토마토는 과일이 아니라 채소다.")
                .answer(Answer.O)
                .difficulty(QuestionDifficulty.EASY)
                .status(QuestionStatus.APPROVED)
                .submitterId(1L)
                .build();
        return questionRepository.save(defaultQ);
    }
}
