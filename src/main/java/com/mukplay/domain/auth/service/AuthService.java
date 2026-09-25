package com.mukplay.domain.auth.service;

import com.mukplay.common.exception.BusinessException;
import com.mukplay.common.exception.ErrorCode;
import com.mukplay.domain.auth.dto.LoginRequest;
import com.mukplay.domain.auth.dto.SignupRequest;
import com.mukplay.domain.auth.dto.TokenResponse;
import com.mukplay.domain.user.entity.User;
import com.mukplay.domain.user.entity.UserRole;
import com.mukplay.domain.user.repository.UserRepository;
import com.mukplay.domain.user.validator.PasswordPolicyValidator;
import com.mukplay.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final JwtProvider jwtProvider;

    @Transactional
    public TokenResponse signup(SignupRequest request) {
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        passwordPolicyValidator.validate(request.password());

        User user = User.builder()
                .loginId(request.loginId())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .role(UserRole.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtProvider.createToken(savedUser.getId(), savedUser.getLoginId(), savedUser.getRole());
        return TokenResponse.of(token, savedUser.getId(), savedUser.getNickname());
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        String token = jwtProvider.createToken(user.getId(), user.getLoginId(), user.getRole());
        return TokenResponse.of(token, user.getId(), user.getNickname());
    }
}
