package com.mukplay.domain.user.controller;

import com.mukplay.common.response.ApiResponse;
import com.mukplay.domain.user.dto.UserProfileResponse;
import com.mukplay.domain.user.service.UserService;
import com.mukplay.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        UserProfileResponse profile = userService.getMyProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}
