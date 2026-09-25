package com.mukplay.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mukplay.domain.auth.dto.LoginRequest;
import com.mukplay.domain.auth.dto.SignupRequest;
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
class AuthControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("정상적인 회원가입 시 201 Created와 JWT 토큰을 반환해야 한다")
    void testSignupSuccess() throws Exception {
        SignupRequest request = new SignupRequest("newuser1", "validPass123", "뉴비플레이어");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.nickname").value("뉴비플레이어"));
    }

    @Test
    @DisplayName("중복된 아이디로 회원가입 시 409 Conflict 및 A006 코드를 반환해야 한다")
    void testDuplicateSignup() throws Exception {
        SignupRequest request = new SignupRequest("dupuser1", "validPass123", "원조유저");
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        SignupRequest duplicateRequest = new SignupRequest("dupuser1", "validPass123", "사칭유저");
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("A006"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("가입된 계정으로 로그인 시 200 OK와 JWT 토큰을 반환해야 한다")
    void testLoginSuccess() throws Exception {
        SignupRequest signup = new SignupRequest("loginuser", "validPass123", "로그인테스트");
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)));

        LoginRequest login = new LoginRequest("loginuser", "validPass123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString());
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 401 Unauthorized 및 A005 코드를 반환해야 한다")
    void testLoginFailure() throws Exception {
        SignupRequest signup = new SignupRequest("loginuser2", "validPass123", "비번오류테스트");
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)));

        LoginRequest wrongLogin = new LoginRequest("loginuser2", "wrongPass999");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A005"))
                .andExpect(jsonPath("$.success").value(false));
    }
}
