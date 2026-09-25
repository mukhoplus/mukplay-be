package com.mukplay.security;

import com.mukplay.domain.user.entity.UserRole;
import com.mukplay.security.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SecurityConfigTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

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
    @DisplayName("인증 없이 보호된 API 호출 시 401 Unauthorized 및 A001 코드를 반환해야 한다")
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A001"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("일반 USER 토큰으로 ADMIN 전용 API 호출 시 403 Forbidden 및 A002 코드를 반환해야 한다")
    void testForbiddenAdminAccess() throws Exception {
        String userToken = jwtProvider.createToken(1L, "regularUser", UserRole.ROLE_USER);

        mockMvc.perform(get("/api/admin/questions")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("A002"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("공개 엔드포인트는 인증 없이도 200 OK로 성공해야 한다")
    void testPublicEndpointAccess() throws Exception {
        mockMvc.perform(get("/api/auth/test"))
                .andExpect(status().isOk());
    }
}

@RestController
@RequestMapping("/api/auth")
class TestAuthController {

    @GetMapping("/test")
    public String test() {
        return "ok";
    }
}
