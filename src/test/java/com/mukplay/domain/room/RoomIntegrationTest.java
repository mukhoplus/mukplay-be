package com.mukplay.domain.room;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mukplay.domain.room.dto.CreateRoomRequest;
import com.mukplay.domain.user.entity.User;
import com.mukplay.domain.user.entity.UserRole;
import com.mukplay.domain.user.repository.UserRepository;
import com.mukplay.security.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class RoomIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private User userA;
    private User userB;
    private User userC;
    private String tokenA;
    private String tokenB;
    private String tokenC;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        userA = userRepository.save(User.builder().loginId("flow_user_a").password("pass").nickname("플레이어A").role(UserRole.ROLE_USER).build());
        userB = userRepository.save(User.builder().loginId("flow_user_b").password("pass").nickname("플레이어B").role(UserRole.ROLE_USER).build());
        userC = userRepository.save(User.builder().loginId("flow_user_c").password("pass").nickname("플레이어C").role(UserRole.ROLE_USER).build());

        tokenA = jwtProvider.createToken(userA.getId(), userA.getLoginId(), userA.getRole());
        tokenB = jwtProvider.createToken(userB.getId(), userB.getLoginId(), userB.getRole());
        tokenC = jwtProvider.createToken(userC.getId(), userC.getLoginId(), userC.getRole());
    }

    @Test
    @DisplayName("전체 Room 라이프사이클 E2E: Create -> List -> Join -> Join -> Leave -> Start")
    void testFullRoomLifecycle() throws Exception {
        // 1. Create Room (User A creates room)
        CreateRoomRequest createReq = new CreateRoomRequest("라이프사이클 검증방", 5);
        MvcResult createResult = mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.hostId").value(userA.getId()))
                .andExpect(jsonPath("$.data.currentPlayers").value(1))
                .andReturn();

        Map<?, ?> responseMap = objectMapper.readValue(createResult.getResponse().getContentAsString(), Map.class);
        Map<?, ?> dataMap = (Map<?, ?>) responseMap.get("data");
        String roomId = (String) dataMap.get("roomId");
        assertThat(roomId).isNotBlank();

        // 2. List Rooms (User B queries rooms)
        mockMvc.perform(get("/api/rooms")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.roomId == '" + roomId + "')].state").value("WAITING"));

        // 3. Join Room (User B joins)
        mockMvc.perform(post("/api/rooms/{roomId}/join", roomId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentPlayers").value(2));

        // 4. Join Room (User C joins)
        mockMvc.perform(post("/api/rooms/{roomId}/join", roomId)
                        .header("Authorization", "Bearer " + tokenC))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentPlayers").value(3));

        // 5. Leave Room (User A leaves -> Host migrated to User B)
        mockMvc.perform(post("/api/rooms/{roomId}/leave", roomId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentPlayers").value(2))
                .andExpect(jsonPath("$.data.hostId").value(userB.getId()));

        // 6. Start Game (New Host User B starts the game)
        mockMvc.perform(post("/api/rooms/{roomId}/start", roomId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state").value("PLAYING"))
                .andExpect(jsonPath("$.data.currentPlayers").value(2));
    }
}
