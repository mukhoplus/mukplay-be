package com.mukplay.domain.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mukplay.domain.auth.dto.LoginRequest;
import com.mukplay.domain.auth.dto.SignupRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class AuthIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("시나리오 1 & 2: 로그인 성공 및 잘못된 비밀번호 실패 검증")
    void testLoginSuccessAndFailure() throws Exception {
        // Given: User exists
        userRepository.save(User.builder()
                .loginId("authuser1")
                .password(passwordEncoder.encode("Password123!"))
                .nickname("인증유저1")
                .role(UserRole.ROLE_USER)
                .build());

        // 1. Success
        LoginRequest successReq = new LoginRequest("authuser1", "Password123!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(successReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString());

        // 2. Failure: Wrong Password
        LoginRequest failReq = new LoginRequest("authuser1", "WrongPassword999");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(failReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A005"));
    }

    @Test
    @DisplayName("시나리오 3: 변조된 JWT 토큰은 401 Unauthorized로 거부되어야 한다")
    void testInvalidJwt() throws Exception {
        String validToken = jwtProvider.createToken(1L, "user1", UserRole.ROLE_USER);
        String tamperedToken = validToken + "invalid-tail";

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + tamperedToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A001"));
    }

    @Test
    @DisplayName("시나리오 4: 만료된 JWT 토큰은 401 Unauthorized로 거부되어야 한다")
    void testExpiredJwt() throws Exception {
        JwtProvider expiredProvider = new JwtProvider(
                "mukplay-super-secure-jwt-secret-key-for-ox-quiz-game-2026-very-long-key",
                -1000
        );
        String expiredToken = expiredProvider.createToken(1L, "user1", UserRole.ROLE_USER);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A001"));
    }

    @Test
    @DisplayName("시나리오 5: USER 권한 토큰으로 본인 정보 정상 조회 (200 OK)")
    void testUserPermission() throws Exception {
        User user = userRepository.save(User.builder()
                .loginId("normaluser")
                .password(passwordEncoder.encode("Pass1234!"))
                .nickname("일반유저")
                .role(UserRole.ROLE_USER)
                .build());

        String token = jwtProvider.createToken(user.getId(), user.getLoginId(), user.getRole());

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.loginId").value("normaluser"))
                .andExpect(jsonPath("$.data.role").value("ROLE_USER"));
    }

    @Test
    @DisplayName("시나리오 6: USER 권한으로 ADMIN 전용 엔드포인트 접근 시 403 Forbidden")
    void testAdminPermissionEnforcement() throws Exception {
        String userToken = jwtProvider.createToken(10L, "normaluser", UserRole.ROLE_USER);

        mockMvc.perform(get("/api/admin/questions")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("A002"));
    }

    @Test
    @DisplayName("시나리오 7: 인증 헤더 없이 보호된 API 접근 시 401 Unauthorized")
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A001"));
    }
}
