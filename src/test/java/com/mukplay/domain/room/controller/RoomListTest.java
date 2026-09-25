package com.mukplay.domain.room.controller;

import com.mukplay.domain.room.dto.CreateRoomRequest;
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

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class RoomListTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomService roomService;

    @Autowired
    private JwtProvider jwtProvider;

    private String userToken;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        User user = userRepository.save(User.builder()
                .loginId("listuser")
                .password("encoded_pass")
                .nickname("목록테스터")
                .role(UserRole.ROLE_USER)
                .build());

        userToken = jwtProvider.createToken(user.getId(), user.getLoginId(), user.getRole());
    }

    @Test
    @DisplayName("GET /api/rooms 호출 시 활성화된 대기방 목록이 반환되어야 한다")
    void testGetWaitingRooms() throws Exception {
        User user = userRepository.findByLoginId("listuser").orElseThrow();
        roomService.createRoom(user.getId(), new CreateRoomRequest("대기방 1번", 8));

        mockMvc.perform(get("/api/rooms")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[?(@.name == '대기방 1번')].state", hasItem("WAITING")))
                .andExpect(jsonPath("$.data[?(@.name == '대기방 1번')].currentPlayers", hasItem(1)));
    }
}
