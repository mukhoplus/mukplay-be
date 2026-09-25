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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class RoomLeaveTest {

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
                .loginId("leavehost")
                .password("pass")
                .nickname("퇴장호스트")
                .role(UserRole.ROLE_USER)
                .build());

        guest = userRepository.save(User.builder()
                .loginId("leaveguest")
                .password("pass")
                .nickname("퇴장게스트")
                .role(UserRole.ROLE_USER)
                .build());

        hostToken = jwtProvider.createToken(host.getId(), host.getLoginId(), host.getRole());
        guestToken = jwtProvider.createToken(guest.getId(), guest.getLoginId(), guest.getRole());
    }

    @Test
    @DisplayName("일반 참가자가 퇴장하면 방 인원이 감소해야 한다")
    void testGuestLeave() throws Exception {
        RoomResponse room = roomService.createRoom(host.getId(), new CreateRoomRequest("퇴장테스트", 5));
        roomService.joinRoom(guest.getId(), room.roomId());

        mockMvc.perform(post("/api/rooms/{roomId}/leave", room.roomId())
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentPlayers").value(1))
                .andExpect(jsonPath("$.data.hostId").value(host.getId()));
    }

    @Test
    @DisplayName("방장이 퇴장하면 다음 참가자에게 방장이 위임되어야 한다")
    void testHostMigrationOnLeave() throws Exception {
        RoomResponse room = roomService.createRoom(host.getId(), new CreateRoomRequest("방장위임테스트", 5));
        roomService.joinRoom(guest.getId(), room.roomId());

        mockMvc.perform(post("/api/rooms/{roomId}/leave", room.roomId())
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentPlayers").value(1))
                .andExpect(jsonPath("$.data.hostId").value(guest.getId()));
    }

    @Test
    @DisplayName("마지막 참가자가 퇴장하면 방이 삭제되어야 한다")
    void testLastParticipantLeaveDeletesRoom() throws Exception {
        RoomResponse room = roomService.createRoom(host.getId(), new CreateRoomRequest("삭제테스트", 5));

        mockMvc.perform(post("/api/rooms/{roomId}/leave", room.roomId())
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk());

        // 방 조회 시 404 확인
        mockMvc.perform(get("/api/rooms/{roomId}", room.roomId())
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("R001"));
    }
}
