package com.mukplay.domain.room.controller;

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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class RoomControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private String userToken;
    private User testUser;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        testUser = userRepository.save(User.builder()
                .loginId("roomcreator")
                .password("encoded_pass")
                .nickname("방장후보")
                .role(UserRole.ROLE_USER)
                .build());

        userToken = jwtProvider.createToken(testUser.getId(), testUser.getLoginId(), testUser.getRole());
    }

    @Test
    @DisplayName("방 생성 API 호출 시 201 Created와 함께 생성자가 호스트로 등록된 방 정보를 반환해야 한다")
    void testCreateRoomSuccess() throws Exception {
        CreateRoomRequest request = new CreateRoomRequest("OX 최강자전", 10);

        mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roomId").isString())
                .andExpect(jsonPath("$.data.name").value("OX 최강자전"))
                .andExpect(jsonPath("$.data.hostId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.currentPlayers").value(1))
                .andExpect(jsonPath("$.data.maxPlayers").value(10))
                .andExpect(jsonPath("$.data.state").value("WAITING"))
                .andExpect(jsonPath("$.data.participants[0].userId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.participants[0].nickname").value("방장후보"));
    }
}
