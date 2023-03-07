package com.gdsc.jmt.domain.user.command.service;

import com.gdsc.jmt.domain.user.command.SignUpCommand;
import com.gdsc.jmt.domain.user.command.LogoutCommand;
import com.gdsc.jmt.domain.user.command.PersistRefreshTokenCommand;
import com.gdsc.jmt.domain.user.command.info.Reissue;
import com.gdsc.jmt.domain.user.common.RoleType;
import com.gdsc.jmt.domain.user.common.SocialType;
import com.gdsc.jmt.domain.user.oauth.info.OAuth2UserInfo;
import com.gdsc.jmt.domain.user.oauth.info.impl.GoogleOAuth2UserInfo;
import com.gdsc.jmt.domain.user.apple.AppleUtil;
import com.gdsc.jmt.global.exception.ApiException;
import com.gdsc.jmt.global.jwt.TokenProvider;
import com.gdsc.jmt.global.jwt.dto.TokenResponse;
import com.gdsc.jmt.global.messege.AuthMessage;
import com.gdsc.jmt.global.messege.UserMessage;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${google.client.id}")
    private String googleClientId;
    private final TokenProvider tokenProvider;
    private final CommandGateway commandGateway;

    @Transactional
    public TokenResponse googleLogin(String idToken) {
        // TODO : GoogleIdTokenVerifier는 Bean으로 등록하고 써도 될듯???
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
//                .setAudience(Collections.singletonList(googleClientId))
                .build();
        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);

            if (googleIdToken == null) {
                throw new ApiException(AuthMessage.INVALID_TOKEN);
            }
            else {
                GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(googleIdToken.getPayload());

                sendSignUpCommend(userInfo, SocialType.GOOGLE);

                return sendGenerateJwtTokenCommend(userInfo.getEmail());
            }
        } catch (IllegalArgumentException | HttpClientErrorException | GeneralSecurityException | IOException e) {
            throw new ApiException(AuthMessage.INVALID_TOKEN);
        }
    }

    @Transactional
    public TokenResponse appleLogin(String idToken) {
        OAuth2UserInfo userInfo = AppleUtil.appleLogin(idToken);

        sendSignUpCommend(userInfo, SocialType.APPLE);
        return sendGenerateJwtTokenCommend(userInfo.getEmail());
    }

    @Transactional
    public TokenResponse reissue(String email, String refreshToken) {
        validateRefreshToken(refreshToken);

        String refreshTokenAggregateId = UUID.randomUUID().toString();
        TokenResponse tokenResponse =createToken(email, refreshTokenAggregateId);
        Reissue reissue = new Reissue(true, refreshToken, tokenResponse.refreshToken());
        commandGateway.sendAndWait(new PersistRefreshTokenCommand(
                refreshTokenAggregateId,
                email,
                null,
                reissue
        ));

        return tokenResponse;
    }

    @Transactional
    public void logout(String email, String refreshToken) {
        validateRefreshToken(refreshToken);

        Claims claims = tokenProvider.parseClaims(refreshToken);
        commandGateway.sendAndWait(new LogoutCommand(
                claims.getSubject(),
                email,
                refreshToken)
        );
    }

    private void sendSignUpCommend(OAuth2UserInfo userInfo, SocialType socialType) {
        String userAggregateId = UUID.randomUUID().toString();
        commandGateway.sendAndWait(new SignUpCommand(
                userAggregateId,
                userInfo,
                socialType));
    }

    private TokenResponse sendGenerateJwtTokenCommend(String email) {
        String refreshTokenAggregateId = UUID.randomUUID().toString();
        TokenResponse tokenResponse = createToken(email, refreshTokenAggregateId);
        commandGateway.sendAndWait(new PersistRefreshTokenCommand(
                refreshTokenAggregateId,
                email,
                tokenResponse.refreshToken(),
                null
        ));
        return tokenResponse;
    }

    private void validateRefreshToken(String refreshToken) {
        if(!tokenProvider.validateToken(refreshToken))
            throw new ApiException(UserMessage.REFRESH_TOKEN_INVALID);
    }

    private TokenResponse createToken(String email, String refreshTokenAggregateId) {
        return tokenProvider.generateJwtToken(email, refreshTokenAggregateId, RoleType.MEMBER);
    }
}
