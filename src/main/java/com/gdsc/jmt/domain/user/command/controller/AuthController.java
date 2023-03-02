package com.gdsc.jmt.domain.user.command.controller;

import com.gdsc.jmt.domain.user.command.dto.LogoutRequest;
import com.gdsc.jmt.domain.user.command.dto.SocialLoginRequest;
import com.gdsc.jmt.domain.user.command.service.AuthService;
import com.gdsc.jmt.global.controller.FirstVersionRestController;
import com.gdsc.jmt.global.dto.ApiResponse;
import com.gdsc.jmt.global.jwt.dto.TokenResponse;
import com.gdsc.jmt.global.messege.UserMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FirstVersionRestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/auth/google")
    public ApiResponse<TokenResponse> googleLogin(@RequestBody SocialLoginRequest socialLoginRequest) {
        TokenResponse tokenResponse = authService.googleLogin(socialLoginRequest.token());
        return ApiResponse.createResponseWithMessage(tokenResponse, UserMessage.LOGIN_SUCCESS);
    }

    @PostMapping("/token")
    public ApiResponse<TokenResponse> reissue(@AuthenticationPrincipal User user, @RequestBody LogoutRequest logoutRequest) {
        TokenResponse tokenResponse = authService.reissue(user.getUsername(), logoutRequest.refreshToken());
        return ApiResponse.createResponseWithMessage(tokenResponse, UserMessage.REISSUE_SUCCESS);
    }

    @DeleteMapping("/user")
    public ApiResponse<?> logout(@AuthenticationPrincipal User user, @RequestBody LogoutRequest logoutRequest) {
        authService.logout(user.getUsername() , logoutRequest.refreshToken());
        return ApiResponse.createResponseWithMessage(null, UserMessage.LOGOUT_SUCCESS);
    }
}
