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
class RoomStartTest {

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
    private String hostToken;
    private String guestToken;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        host = userRepository.save(User.builder()
                .loginId("starthost")
                .password("pass")
                .nickname("시작방장")
                .role(UserRole.ROLE_USER)
                .build());

        guest = userRepository.save(User.builder()
                .loginId("startguest")
                .password("pass")
                .nickname("시작게스트")
                .role(UserRole.ROLE_USER)
                .build());

        hostToken = jwtProvider.createToken(host.getId(), host.getLoginId(), host.getRole());
        guestToken = jwtProvider.createToken(guest.getId(), guest.getLoginId(), guest.getRole());
    }

    @Test
    @DisplayName("방장이 아닌 유저가 게임 시작 요청 시 403 Forbidden 및 NOT_ROOM_HOST 코드를 반환해야 한다")
    void testNonHostCannotStart() throws Exception {
        RoomResponse room = roomService.createRoom(host.getId(), new CreateRoomRequest("시작테스트", 5));
        roomService.joinRoom(guest.getId(), room.roomId());

        mockMvc.perform(post("/api/rooms/{roomId}/start", room.roomId())
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("R004"));
    }

    @Test
    @DisplayName("2명 미만일 때 게임 시작 요청 시 400 Bad Request를 반환해야 한다")
    void testCannotStartWithSinglePlayer() throws Exception {
        RoomResponse room = roomService.createRoom(host.getId(), new CreateRoomRequest("1인방", 5));

        mockMvc.perform(post("/api/rooms/{roomId}/start", room.roomId())
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    @DisplayName("2명 이상 참여 상태에서 방장이 시작하면 방 상태가 PLAYING으로 변경되어야 한다")
    void testStartGameSuccess() throws Exception {
        RoomResponse room = roomService.createRoom(host.getId(), new CreateRoomRequest("시작성공방", 5));
        roomService.joinRoom(guest.getId(), room.roomId());

        mockMvc.perform(post("/api/rooms/{roomId}/start", room.roomId())
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.state").value("PLAYING"));
    }
}
