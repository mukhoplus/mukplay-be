package com.mukplay.domain.user.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class UserControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

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
    @DisplayName("유효한 JWT로 GET /api/users/me 호출 시 본인 프로필 정보를 반환해야 한다")
    void testGetMyProfileSuccess() throws Exception {
        User user = User.builder()
                .loginId("meuser")
                .password("encoded_pass")
                .nickname("프로필주인")
                .role(UserRole.ROLE_USER)
                .build();
        user.addExp(250); // Level 3, exp 250
        User savedUser = userRepository.save(user);

        String token = jwtProvider.createToken(savedUser.getId(), savedUser.getLoginId(), savedUser.getRole());

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.data.loginId").value("meuser"))
                .andExpect(jsonPath("$.data.nickname").value("프로필주인"))
                .andExpect(jsonPath("$.data.level").value(3))
                .andExpect(jsonPath("$.data.exp").value(250));
    }

    @Test
    @DisplayName("인증 헤더 없이 호출 시 401 Unauthorized를 반환해야 한다")
    void testGetMyProfileUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A001"));
    }
}
