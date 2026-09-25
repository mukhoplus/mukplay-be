package com.mukplay.domain.room.controller;

import com.mukplay.domain.room.dto.CreateRoomRequest;
import com.mukplay.domain.room.dto.RoomResponse;
import com.mukplay.domain.room.service.RoomService;
import com.mukplay.domain.user.entity.User;
import com.mukplay.domain.user.entity.UserRole;
import com.mukplay.domain.user.repository.UserRepository;
import com.mukplay.security.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class RoomJoinTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomService roomService;

    @Autowired
    private JwtProvider jwtProvider;

    private User host;
    private User guest;
    private String guestToken;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        host = userRepository.save(User.builder()
                .loginId("joinhost")
                .password("encoded_pass")
                .nickname("입장호스트")
                .role(UserRole.ROLE_USER)
                .build());

        guest = userRepository.save(User.builder()
                .loginId("joinguest")
                .password("encoded_pass")
                .nickname("입장게스트")
                .role(UserRole.ROLE_USER)
                .build());

        guestToken = jwtProvider.createToken(guest.getId(), guest.getLoginId(), guest.getRole());
    }

    @Test
    @DisplayName("정상적인 방 입장 시 200 OK와 참가자가 추가된 방 정보를 반환해야 한다")
    void testJoinRoomSuccess() throws Exception {
        RoomResponse created = roomService.createRoom(host.getId(), new CreateRoomRequest("참가 테스트방", 5));

        mockMvc.perform(post("/api/rooms/{roomId}/join", created.roomId())
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentPlayers").value(2))
                .andExpect(jsonPath("$.data.participants[?(@.userId == " + guest.getId() + ")].nickname").value("입장게스트"));
    }

    @Test
    @DisplayName("이미 참가 중인 유저의 중복 입장 시 409 Conflict 및 ALREADY_JOINED 코드를 반환해야 한다")
    void testDuplicateJoin() throws Exception {
        RoomResponse created = roomService.createRoom(host.getId(), new CreateRoomRequest("중복 입장방", 5));
        roomService.joinRoom(guest.getId(), created.roomId());

        mockMvc.perform(post("/api/rooms/{roomId}/join", created.roomId())
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("R005"));
    }

    @Test
    @DisplayName("정원이 꽉 찬 방에 입장 시 400 Bad Request 및 ROOM_FULL 코드를 반환해야 한다")
    void testRoomCapacityFull() throws Exception {
        RoomResponse created = roomService.createRoom(host.getId(), new CreateRoomRequest("2인 전용방", 2));
        roomService.joinRoom(guest.getId(), created.roomId());

        User thirdUser = userRepository.save(User.builder()
                .loginId("thirduser")
                .password("pass")
                .nickname("제3자")
                .build());
        String thirdToken = jwtProvider.createToken(thirdUser.getId(), thirdUser.getLoginId(), thirdUser.getRole());

        mockMvc.perform(post("/api/rooms/{roomId}/join", created.roomId())
                        .header("Authorization", "Bearer " + thirdToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("R002"));
    }

    @Test
    @DisplayName("존재하지 않는 방에 입장 시 404 Not Found 및 ROOM_NOT_FOUND 코드를 반환해야 한다")
    void testRoomNotFound() throws Exception {
        mockMvc.perform(post("/api/rooms/{roomId}/join", "non-existent-room-id")
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("R001"));
    }
}
